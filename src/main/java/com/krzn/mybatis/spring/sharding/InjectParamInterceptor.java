package com.krzn.mybatis.spring.sharding;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import com.krzn.mybatis.spring.sharding.annotation.DynamicParam;
import com.krzn.mybatis.spring.sharding.annotation.InjectParams;
import com.krzn.mybatis.spring.sharding.annotation.Sharding;
import com.krzn.mybatis.spring.sharding.annotation.ShardingTable;
import com.krzn.mybatis.spring.sharding.core.ClassBody;
import com.krzn.mybatis.spring.sharding.core.TableShardingHolder;
import com.krzn.mybatis.spring.sharding.datasource.DataSourceHolder;
import com.krzn.mybatis.spring.sharding.support.DefaultTableNameHandler;
import com.krzn.mybatis.spring.sharding.support.DynamicValueStrategy;
import com.krzn.mybatis.spring.sharding.util.StringTemplate;

/**
 * Mybatis动态参数注入插件
 * 
 * @author yanhuajian
 * @date:2016年4月1日 下午11:46:51
 * @version V1.0
 * 
 */
@Intercepts({
		@Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class }),
		@Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }),
		@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class, RowBounds.class,
				ResultHandler.class, CacheKey.class, BoundSql.class }),
		@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class, RowBounds.class,
				ResultHandler.class }), })
public class InjectParamInterceptor implements Interceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(InjectParamInterceptor.class);

	private static final ObjectFactory OBJECT_FACTORY = new DefaultObjectFactory();
	private static final ObjectWrapperFactory OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
	private static final ReflectorFactory REFLECTOR_FACTORY = new DefaultReflectorFactory();

	private static final DefaultTableNameHandler TABLE_NAME_HANDLER = new DefaultTableNameHandler();
	
	@Override
	public Object intercept(Invocation invocation) throws Throwable {

		Object targetObject = invocation.getTarget();

		/**
		 * 这里主要是做出数据源的选择,80%代码和武洪的分库逻辑是一样的。。。
		 */
		if (targetObject instanceof Executor) {
			
			// 如果没有绑定这个线程来手工分表，则不执行下面逻辑
			Integer tableIndex = DataSourceHolder.getTableIndex();
			if (tableIndex == null) {
				return invocation.proceed();
			}
			
			MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
			// 创建Java类体对象，获取当前执行的类信息
			ClassBody classBody = this.createClassBody(mappedStatement);

			// 取得当前表名前缀
			String tablePrefix = null;
			// 先获取方法上的@Sharding注解
			Sharding shardingMeta = classBody.getMethod().getAnnotation(Sharding.class);
			if (shardingMeta != null) {
				tablePrefix = shardingMeta.tablePrefix();
			}

			// 再尝试获取类上面的ShardingTable注解。
			if (tablePrefix == null || tablePrefix.trim().length() == 0) {
				ShardingTable shardingTableMeta = classBody.getClassObject().getAnnotation(ShardingTable.class);
				if (shardingTableMeta != null) {
					tablePrefix = shardingTableMeta.tablePrefix();
				}
			}
			if (tablePrefix == null || tablePrefix.trim().length() == 0) {
				LOGGER.error("必须在方法[{}]上的@Sharding中或者类[{}]上的@ShardingTable中指定tablePrefix!", classBody.getMethod(),
						classBody.getClassObject());
				throw new IllegalArgumentException("tablePrefix can't be null");
			}

			// 获取分表元信息。
			TableShardingBean shardingBean = TableShardingHolder.getTableShardingInfos().get(tablePrefix);
			if (shardingBean == null) {
				LOGGER.error("没有表[{}]对应的分表信息!", tablePrefix);
				throw new IllegalArgumentException("shardinginfo can't be null");
			}
			// 计算分表号和分库号。
			int shardingTableCount = shardingBean.getShardingTableCount();
			int realTableCount = shardingBean.getRealTableCount();
			int shardingDBCount = shardingBean.getShardingDBCount();
			
			// 计算系数(理论单库中的表个数/实际单库中的表个数)
			int factor = (shardingTableCount / shardingDBCount) / (realTableCount / shardingDBCount);

			// 计算分库号
			int shardingDBNo = (tableIndex * factor) / (shardingTableCount / shardingDBCount);
			
			// 拿到表号
			int shardingTableNo = tableIndex;
			String realTableIndex = TABLE_NAME_HANDLER.generateRealTableNo(realTableCount, shardingTableNo);
			
			// 设置真实的表号
			DataSourceHolder.bindRealTableIndex(realTableIndex);

			// 设置数据库源
			DataSourceHolder.bindDataSource(shardingDBNo);
		}

		/**
		 * 这里主要是替换SQL中的模板
		 */
		if (targetObject instanceof StatementHandler) {

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

//			Configuration configuration = (Configuration) metaStatementHandler.getValue("delegate.configuration");
			MappedStatement mappedStatement = (MappedStatement) metaStatementHandler.getValue("delegate.mappedStatement");
			BoundSql boundSql = (BoundSql) metaStatementHandler.getValue("delegate.boundSql");
//			RowBounds rowBounds = (RowBounds) metaStatementHandler.getValue("delegate.rowBounds");

			// 创建Java类体对象，获取当前执行的类信息
			ClassBody classBody = this.createClassBody(mappedStatement);

			// 根据方法上的@InjectParam注解进行逻辑处理
			InjectParams injectParams = classBody.getMethod().getAnnotation(InjectParams.class);
			if (injectParams != null) {
				Map<String, String> tokens = parserParams(injectParams.params());

				if (!CollectionUtils.isEmpty(tokens)) {
					// 取得原SQL语句
					String sql = boundSql.getSql();
					LOGGER.debug("sql = [{}]", sql);

					// 替换sql中自定义的模板
					String newSql = StringTemplate.replace(sql, tokens);

					metaStatementHandler.setValue("delegate.boundSql.sql", newSql);
				}
			}

		}
		return invocation.proceed();

	}

	/**
	 * 解析DynamicParam中的key/value成为一个map对象
	 * 
	 * @param
	 * @return
	 * @throws Exception
	 * @author: yanhuajian 2016年4月2日下午7:14:14
	 */
	private Map<String, String> parserParams(DynamicParam[] params) throws Exception {
		Map<String, String> tokens = null;
		if (params != null && params.length > 0) {
			tokens = new HashMap<String, String>();
			for (DynamicParam p : params) {
				String key = p.key();
				Object value = null;
				Class<? extends DynamicValueStrategy> valueClass = p.value();
				DynamicValueStrategy valueStrategy = valueClass.newInstance();
				value = valueStrategy.get(key);

				tokens.put(key, value.toString());
			}
		}
		return tokens;
	}

	/**
	 * 创建Java类体对象，获取当前执行的类信息
	 * 
	 * @param mappedStatement
	 * @return
	 * @throws Exception
	 * @author: yanhuajian 2016年4月2日下午7:10:54
	 */
	private ClassBody createClassBody(MappedStatement mappedStatement) throws Exception {
		// 获取Mapper中执行的方法ID
		String methodInfo = mappedStatement.getId();
		// 通过Mapper的方法ID截取Dao的className和methodName
		int splitIndex = methodInfo.lastIndexOf(".");
		String className = methodInfo.substring(0, splitIndex);
		String methodName = methodInfo.substring(splitIndex + 1);
		// 通过反射取得Dao的Class和Method
		Class<?> classObject = Class.forName(className);
		Class<?>[] paramTypes = null; // 无法获取实际方法参数列表，这里传null。
		Method method = ReflectionUtils.findMethod(classObject, methodName, paramTypes);

		return new ClassBody(classObject, method, paramTypes);
	}

	// 实现plugin方法时判断一下目标类型，是本插件要拦截的对象才执行Plugin.wrap方法，否者直接返回目标本省，这样可以减少目标被代理的次数。
	@Override
	public Object plugin(Object target) {
		// 拦截StatementHandler
		if (target instanceof Executor) {
			return Plugin.wrap(target, this);
		} else if (target instanceof StatementHandler) {
			return Plugin.wrap(target, this);
		} else {
			return target;
		}
	}

	@Override
	public void setProperties(Properties properties) {
	}

}