/*
 * Copyright (c) 2016-2020 LEJR.COM All Right Reserved
 */

package com.krzn.mybatis.spring.sharding;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ReflectionUtils;

import com.krzn.mybatis.spring.sharding.ShardingStrategy.NumberPair;
import com.krzn.mybatis.spring.sharding.annotation.NonSharding;
import com.krzn.mybatis.spring.sharding.annotation.Sharding;
import com.krzn.mybatis.spring.sharding.annotation.ShardingTable;
import com.krzn.mybatis.spring.sharding.core.TableShardingHolder;
import com.krzn.mybatis.spring.sharding.datasource.DataSourceLocalKeys;
import com.krzn.mybatis.spring.sharding.support.DefaultShadowTableNameHandler;
import com.krzn.mybatis.spring.sharding.support.DefaultShardingDataSourceNameGenerator;
import com.krzn.mybatis.spring.sharding.support.DefaultShardingStrategy;
import com.krzn.mybatis.spring.sharding.support.DefaultTableNameHandler;
import com.krzn.mybatis.spring.sharding.support.SimpleShardingValueStrategy;

/**
 * 针对Mybatis的分库分表、读写分离插件。
 * <p>这个插件的处理逻辑是做代理拦截Executor和StatementHandler。
 * <p>在拦截Executor的时候，mybatis的机制还没有做数据源的选择，
 * 所以这个时机用来选择数据源(分库)。
 * <p>在拦截StatementHandler的时候来做表名替换的逻辑(分表)。
 * 
 * @author WuHong
 * @version 1.0 
 * @date 2016年3月16日
 */
@Intercepts({ 
	@Signature(
			type = StatementHandler.class, 
			method = "prepare", 
			args = {Connection.class}
			),
			@Signature(
					type = Executor.class, 
					method = "update", 
					args = {MappedStatement.class, Object.class}
					),
					@Signature(
							type = Executor.class, 
							method = "query", 
							args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}
							),
							@Signature(
									type = Executor.class, 
									method = "query", 
									args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
									),
})
public class ShardingInterceptor implements Interceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ShardingInterceptor.class);

	private static final ShardingValueStrategy DEFAULT_SHARDING_VALUE_STRATEGY = 
			new SimpleShardingValueStrategy();
	
	/**
	 * 默认拆分策略。
	 */
	private static final ShardingStrategy DEFAULT_SHARDINGSTRATEGY = 
			new DefaultShardingStrategy();

	/**
	 * 分表表名处理器。
	 */
	private TableNameHandler tableNameHandler = new DefaultTableNameHandler();
	
	/**
	 * 影子表表名处理器。
	 */
	private TableNameHandler shadowTableNameHandler = new DefaultShadowTableNameHandler(tableNameHandler);
	
	/**
	 * 表示是否可测试。
	 */
	private boolean isTestable = false;
	
	/**
	 * 数据源名称生成器。
	 */
	private ShardingDataSourceNameGenerator dataSourceNameGenerator =
			new DefaultShardingDataSourceNameGenerator();
	
	private static final ObjectFactory OBJECT_FACTORY = new DefaultObjectFactory();
	private static final ObjectWrapperFactory OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
	private static final ReflectorFactory REFLECTOR_FACTORY = new DefaultReflectorFactory();

	/*
	 * 下面两个属性用于在Executor代理和StatementHandler代理
	 * 之间传递分表信息。
	 * 由于在处理这两个代理的时候，都会重新从Sqlsource中来构建
	 * BoundSql，也无法修改Sqlsource这份源数据，所以这里通过
	 * ThreadLocal来做这个传递。
	 */
	private static final ThreadLocal<String> LOCAL_TABLEPREFIX = new ThreadLocal<String>();
	private static final ThreadLocal<String> LOCAL_REALTABLENAME = new ThreadLocal<String>();

	public Object intercept(Invocation invocation) throws Throwable {
		Object targetObject = invocation.getTarget();
		if(targetObject instanceof Executor){
			MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
			Object parameterObject = invocation.getArgs()[1];
			//获取Dao执行的方法信息。 例如：com.a.b.UserDao.addUser
			String methodInfo = mappedStatement.getId();
			//获取Dao层面的目标执行方法。
			int splitIndex = methodInfo.lastIndexOf(".");
			String className = methodInfo.substring(0, splitIndex);
			String methodName = methodInfo.substring(splitIndex + 1);
			Class<?> classObject = Class.forName(className);
			/*
			 * 通过方法名称查找方法实例。
			 * 注意这里有一个问题，没办法处理重载方法。
			 */
			Class<?>[] paramTypes = null;//无法获取实际方法参数列表，这里传null。
			Method method = ReflectionUtils.findMethod(classObject, methodName, paramTypes);
			//根据方法上的@Sharding注解进行逻辑处理。
			Sharding shardingMeta = method.getAnnotation(Sharding.class);
			if(shardingMeta != null){
				String tablePrefix = shardingMeta.tablePrefix();
				if(tablePrefix == null || tablePrefix.trim().length() == 0){
					//再尝试获取类上面的ShardingTable注解。
					ShardingTable shardingTableMeta = classObject.getAnnotation(ShardingTable.class);
					if(shardingTableMeta != null){
						tablePrefix = shardingTableMeta.tablePrefix();
					}
				}
				if(tablePrefix == null || tablePrefix.trim().length() == 0){
					LOGGER.error("必须在方法[{}]上的@Sharding中或者类[{}]上的@ShardingTable中指定tablePrefix!", method, classObject);
					throw new IllegalArgumentException("tablePrefix can't be null");
				}
				String property = shardingMeta.property();
				if(property == null || property.trim().length() == 0){
					LOGGER.error("方法[{}]上的@Sharding必须指定property!", method);
					throw new IllegalArgumentException("property can't be null");
				}
				Class<? extends ShardingValueStrategy> svsClass = shardingMeta.shardingValueStrategy();
				ShardingValueStrategy valueStrategy = null;
				if(svsClass == null){
					valueStrategy = DEFAULT_SHARDING_VALUE_STRATEGY;
				}else{
					valueStrategy = svsClass.newInstance();
				}
				//获取分表元信息。
				TableShardingBean shardingBean = TableShardingHolder.getTableShardingInfos().get(tablePrefix);
				if(shardingBean == null){
					LOGGER.error("没有表[{}]对应的分表信息!", tablePrefix);
					throw new IllegalArgumentException("shardinginfo can't be null");
				}
				LOGGER.debug("针对于[{}]的分表信息为[{}]！", tablePrefix, shardingBean);
				BoundSql boundSql = mappedStatement.getBoundSql(parameterObject);
				//1.通过Sharding值来计算分表号和分库号。
				//1.1计算Sharding值。
				Object shardingValue = computeShardingValue(valueStrategy, property, boundSql);
				//1.2计算分表号和分库号。
				//计算分表号和分库号。 
				int shardingTableCount = shardingBean.getShardingTableCount();
				int shardingDBCount = shardingBean.getShardingDBCount();
				ShardingStrategy shardingStrategy = shardingBean.getShardingStrategy();
				if(shardingStrategy == null){
					shardingStrategy = DEFAULT_SHARDINGSTRATEGY;
				}
				NumberPair pair = shardingStrategy.sharding(shardingValue, shardingTableCount, shardingDBCount);
				LOGGER.debug("根据分库分表值[{}]算出来的库表号信息为:[{}]", shardingValue, pair);
				//2.准备替换表名。
				//2.1获取原始sql。
				String originalSql = boundSql.getSql();
				LOGGER.debug("originalSql = [{}]", originalSql);
				//2.2生成实际物理表名。
				//如果指定了真实分表数,那么按照真实分表数来生成表名。
				int realTableCount = shardingBean.getRealTableCount();
				if(realTableCount == 0){
					realTableCount = shardingTableCount;
				}
				//判断影子表逻辑。
				String realTableName = null;
				//如果测试开关打开，且设置了影子表本地变量，才会生成影子表名。
				if(isTestable && TestSupports.isShadowTableChoosed()){
					realTableName = shadowTableNameHandler.generateRealTableName(tablePrefix, pair.getTableNo(), realTableCount);
				}else{
					realTableName = tableNameHandler.generateRealTableName(tablePrefix, pair.getTableNo(), realTableCount);
				}
				//2.3将表前缀和实际物理表名存到线程上下文。
				LOCAL_TABLEPREFIX.set(tablePrefix);
				LOCAL_REALTABLENAME.set(realTableName);
				//3.指定数据源。
				//3.1指定虚拟数据源。
				String vdsKey = shardingBean.getVdsName();
				LOGGER.debug("虚拟数据源Key={}", vdsKey);
				DataSourceLocalKeys.CURRENT_VDS_KEY.set(vdsKey);
				//3.2通过分库号指定数据源组。
				String dsGroupKey = dataSourceNameGenerator.generate(pair.getDataSourceNo(), shardingDBCount);
				DataSourceLocalKeys.CURRENT_DS_GROUP_KEY.set(dsGroupKey);
				//3.3指定读写分离标志。
				RW rw = shardingMeta.rw();
				if(RW.READ.equals(rw)){
					DataSourceLocalKeys.chooseSlave();
				}else if(RW.WRITE.equals(rw)){
					DataSourceLocalKeys.chooseMaster();
				}else{
					//如果当前处于数据库事务中,那么选择主库。
					if(TransactionSynchronizationManager.isActualTransactionActive()
							&& !TransactionSynchronizationManager.isCurrentTransactionReadOnly()){
						DataSourceLocalKeys.chooseMaster();
					}else{
						//否则，通过Sql来判断。
						SqlCommandType commandType = mappedStatement.getSqlCommandType();
						if(SqlCommandType.SELECT.equals(commandType)){
							DataSourceLocalKeys.chooseSlave();
						}else{
							DataSourceLocalKeys.chooseMaster();
						}
					}
				}
				return invocation.proceed();
			}else{
				//根据方法上的@NonSharding注解进行逻辑处理。
				NonSharding nonShardingMeta = method.getAnnotation(NonSharding.class);
				if(nonShardingMeta != null){
					//指定手工数据源组。
					String manualDSGKey = nonShardingMeta.manualDataSourceGroupKey();
					if(manualDSGKey != null && manualDSGKey.trim().length() > 0){
						DataSourceLocalKeys.CURRENT_MANUAL_DS_GROUP_KEY.set(manualDSGKey);
					}else{
						DataSourceLocalKeys.CURRENT_MANUAL_DS_GROUP_KEY.remove();
					}
					//指定读写分离标志。
					RW rw = nonShardingMeta.rw();
					if(RW.READ.equals(rw)){
						DataSourceLocalKeys.chooseSlave();
					}else if(RW.WRITE.equals(rw)){
						DataSourceLocalKeys.chooseMaster();
					}else{
						//如果当前处于数据库事务中,那么选择主库。
						if(TransactionSynchronizationManager.isActualTransactionActive()
								&& !TransactionSynchronizationManager.isCurrentTransactionReadOnly()){
							DataSourceLocalKeys.chooseMaster();
						}else{
							//否则，通过Sql来判断。
							SqlCommandType commandType = mappedStatement.getSqlCommandType();
							if(SqlCommandType.SELECT.equals(commandType)){
								DataSourceLocalKeys.chooseSlave();
							}else{
								DataSourceLocalKeys.chooseMaster();
							}
						}
					}
				}
				DataSourceLocalKeys.CURRENT_VDS_KEY.remove();
				DataSourceLocalKeys.CURRENT_DS_GROUP_KEY.remove();
			}
		}

		if(targetObject instanceof StatementHandler){
			//拦截StatementHandler做分表。
			StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
			MetaObject metaStatementHandler = MetaObject.forObject(statementHandler, OBJECT_FACTORY, OBJECT_WRAPPER_FACTORY,
					REFLECTOR_FACTORY);

			// 分离代理对象链(由于目标类可能被多个拦截器拦截，从而形成多次代理，通过下面的两次循环可以分离出最原始的的目标类)
			while (metaStatementHandler.hasGetter("h")) {
				Object object = metaStatementHandler.getValue("h");
				metaStatementHandler = MetaObject.forObject(object, OBJECT_FACTORY, OBJECT_WRAPPER_FACTORY, REFLECTOR_FACTORY);
			}
			// 分离最后一个代理对象的目标类
			while (metaStatementHandler.hasGetter("target")) {
				Object object = metaStatementHandler.getValue("target");
				metaStatementHandler = MetaObject.forObject(object, OBJECT_FACTORY, OBJECT_WRAPPER_FACTORY, REFLECTOR_FACTORY);
			}

			BoundSql boundSql = (BoundSql) metaStatementHandler.getValue("delegate.boundSql");
			
			String originalSql = boundSql.getSql();
			LOGGER.debug("originalSql = [{}]", originalSql);
			String tablePrefix = LOCAL_TABLEPREFIX.get();
			String realTableName = LOCAL_REALTABLENAME.get();
			LOCAL_TABLEPREFIX.remove();
			LOCAL_REALTABLENAME.remove();
			if(tablePrefix == null || realTableName == null){
				LOGGER.debug("no tablePrefix or realTableName in threadlocal!");
				return invocation.proceed();
			}else{
				String newSql = originalSql.replaceAll(tablePrefix, realTableName);
				LOGGER.debug("newSql = [{}]", newSql);
				metaStatementHandler.setValue("delegate.boundSql.sql", newSql); 
				return invocation.proceed();
			}
		}
		return invocation.proceed();
	}

	private Object computeShardingValue(ShardingValueStrategy valueStrategy, String property, BoundSql boundSql) {
		List<ParameterMapping> mappings = boundSql.getParameterMappings();
		Object parameterObject = boundSql.getParameterObject();
		Class<?> clazz = parameterObject.getClass();
		Object propertyValue = null;
		if(clazz == String.class){
			propertyValue = (String)parameterObject;
		}else if(clazz == Integer.class
				|| clazz == int.class
				|| clazz == Long.class
				|| clazz == long.class
				|| clazz == Short.class
				|| clazz == short.class
				|| clazz == Byte.class
				|| clazz == byte.class
				|| clazz == Double.class
				|| clazz == double.class
				|| clazz == Float.class
				|| clazz == float.class){
			propertyValue = parameterObject;
		}else if(clazz.equals(Array.class)){
			for(int i=0;i<mappings.size();i++){
				ParameterMapping mapping = mappings.get(i);
				String columnName = mapping.getProperty();
				if(property.equals(columnName)){
					propertyValue = Array.get(parameterObject, i);
					break;
				}
			}
		}else{
			//FIXME 目前先简单实现，没考虑复杂情况。
			MetaObject metaObject = SystemMetaObject.forObject(parameterObject);
			propertyValue = metaObject.getValue(property);
		}


		if(propertyValue == null){
			LOGGER.error("无法获取参数[{}]的值!", property);
			throw new RuntimeException("can't get the value of "+ property);
		}

		return valueStrategy.get(propertyValue);
	}

	public Object plugin(Object target) {
		if (target instanceof StatementHandler) {  
			return Plugin.wrap(target, this);  
		}else if(target instanceof Executor) {  
			return Plugin.wrap(target, this);  
		}else{
			return target;
		}
	}

	public void setProperties(Properties properties) {
		//do nothing！
	}

	public void setTableNameHandler(TableNameHandler tableNameHandler) {
		this.tableNameHandler = tableNameHandler;
		//设置影子表处理器。
		this.shadowTableNameHandler = new DefaultShadowTableNameHandler(tableNameHandler);
	}
	
	public void setTestable(boolean isTestable) {
		this.isTestable = isTestable;
	}

	public void setDataSourceNameGenerator(
			ShardingDataSourceNameGenerator dataSourceNameGenerator) {
		this.dataSourceNameGenerator = dataSourceNameGenerator;
	}

}
