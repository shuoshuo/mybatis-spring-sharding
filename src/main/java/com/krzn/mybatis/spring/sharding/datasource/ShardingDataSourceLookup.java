/*
 * Copyright (c) 2016-2020 LEJR.COM All Right Reserved
 */

package com.krzn.mybatis.spring.sharding.datasource;

import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.DataSourceLookup;
import org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException;

/**
 * 支持分库分表，读写分离的数据源查找器。
 * 
 * @author WuHong
 * @version 1.0 
 * @date 2016年3月17日
 */
public class ShardingDataSourceLookup implements DataSourceLookup{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ShardingDataSourceLookup.class);
	
	/**
	 * 默认从数据源的负载策略。
	 */
	private static final LoadBalance DEFAULT_LOAD_BALANCE = new RandomLoadBalance();
	
	/**
	 * 保存数据源组的映射。
	 */
	private Map<String, VirtualDataSource> virtualDataSourceMap;
	
	/**
	 * 用于支持一些非分库分表的数据源的映射。
	 */
	private Map<String, DataSourceGroup> noShardingDataSourceGroupMap;
	
	/**
	 * 如果没有指定从noShardingDataSourceGroupMap中寻找数据源，
	 * 可以使用这个作为backup。
	 */
	private DataSourceGroup defaultNoShardingDataSourceGroup;
	
	public DataSource getDataSource(String dataSourceName)
			throws DataSourceLookupFailureException {
		try {
			//先获取一个虚拟数据源。
			VirtualDataSource vds = null;
			if(virtualDataSourceMap != null && dataSourceName != null){
				vds = virtualDataSourceMap.get(dataSourceName);
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("通过[DataSourceName={}]选择了一个虚拟数据源[{}]!", dataSourceName, vds);
				}
			}
			//目标数据源组。
			DataSourceGroup  targetDSGroup = null;
			if(vds == null){
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("根据非分库分表策略来选择数据源...");
				}
				//非分库分表的处理策略，通过手工指定的分库号来获取一个数据源组。
				if(noShardingDataSourceGroupMap != null){
					String groupKey = DataSourceLocalKeys.CURRENT_MANUAL_DS_GROUP_KEY.get();
					if(groupKey != null){
						targetDSGroup = noShardingDataSourceGroupMap.get(groupKey);
					}
				}
				if(targetDSGroup == null){
					targetDSGroup = defaultNoShardingDataSourceGroup;
				}
				if(targetDSGroup == null){
					throw new IllegalStateException("找不到非分库分表处理策略下合适的数据源组!");
				}
			}else{
				//通过具体分库分表策略计算得出的分库号来获取一个数据源组。
				String groupKey = DataSourceLocalKeys.CURRENT_DS_GROUP_KEY.get();
				if(groupKey != null){
					targetDSGroup = vds.getDataSourceGroup(groupKey);
				}
				if(targetDSGroup == null){
					throw new IllegalStateException("找不到数据源组key=["+groupKey+"]对应的数据源组!");
				}
			}
			
			//读写分离。
			if(DataSourceLocalKeys.MASTER_KEY
					.equals(DataSourceLocalKeys.CURRENT_MASTER_SLAVE_KEY.get())){
				return targetDSGroup.getMasterDataSource();
			}else{
				List<DataSource> slaveDataSources = targetDSGroup.getSlaveDataSources();
				//如果没有配置从库，返回主库数据源。
				if(slaveDataSources == null || slaveDataSources.size() == 0){
					return targetDSGroup.getMasterDataSource();
				}
				//根据负载策略选择一个从库数据源。
				else{
					LoadBalance loadBalance = targetDSGroup.getLoadBalance();
					if(loadBalance == null){
						loadBalance = DEFAULT_LOAD_BALANCE;
					}
					return loadBalance.select(targetDSGroup.getSlaveDataSources());
				}
			}
		} catch (Exception e) {
			LOGGER.error("选择数据源过程中发生错误!", e);
			throw new DataSourceLookupFailureException(e.getMessage(), e);
		} finally{
			DataSourceLocalKeys.CURRENT_DS_GROUP_KEY.remove();
			DataSourceLocalKeys.CURRENT_MANUAL_DS_GROUP_KEY.remove();
			DataSourceLocalKeys.CURRENT_MASTER_SLAVE_KEY.remove();
			DataSourceLocalKeys.CURRENT_VDS_KEY.remove();
		}
	}

	public void setVirtualDataSourceMap(
			Map<String, VirtualDataSource> virtualDataSourceMap) {
		this.virtualDataSourceMap = virtualDataSourceMap;
	}

	public void setNoShardingDataSourceGroupMap(Map<String, DataSourceGroup> noShardingDataSourceGroupMap) {
		this.noShardingDataSourceGroupMap = noShardingDataSourceGroupMap;
	}

	public void setDefaultNoShardingDataSourceGroup(DataSourceGroup defaultNoShardingDataSourceGroup) {
		this.defaultNoShardingDataSourceGroup = defaultNoShardingDataSourceGroup;
	}
	
}
