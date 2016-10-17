/*
 * Copyright (c) 2016-2020 LEJR.COM All Right Reserved
 */

package com.krzn.mybatis.spring.sharding.datasource;

import java.util.List;
import java.util.Map;

/**
 * 虚拟数据源。
 * <p>一个虚拟数据源对应多个数据源组。
 * <p>一个数据源组下包含一套主从。
 * 
 * <pre>
 * VDS----- DSGroup-0 ----- DS-M
 *      |               |
 *      |               --- DS-S-0
 *      |               |
 *      |               --- DS-S-1
 *      |               |
 *      |               ...
 *      |               
 *      --- DSGroup-1 ----- DS-M
 *      |               |
 *      |               --- DS-S-0
 *      |               |
 *      |               --- DS-S-1
 *      |               |
 *      |               ...
 *      |
 *      ...
 * </pre>
 * 
 * @author WuHong
 * @version 1.0 
 * @date 2016年3月18日
 * 
 * @see DataSourceGroup
 */
public class VirtualDataSource {
	
	/**
	 * 名称。
	 */
	private String name;

	/**
	 * 数据源组Map。
	 */
	private Map<String, DataSourceGroup> dataSourceGroupMap;
	
	/**
	 * 分库数量。
	 * <p>等于数据源组个数。
	 */
	private int shardingDBCount;
	
	/**
	 * 分表信息。
	 */
	private List<ShardingTableInfo> shardingTableInfos;
	
	public DataSourceGroup getDataSourceGroup(String key){
		if(dataSourceGroupMap == null){
			return null;
		}
		return dataSourceGroupMap.get(key);
	}
	
	public int getShardingDBCount() {
		return shardingDBCount;
	}

	public void setDataSourceGroupMap(
			Map<String, DataSourceGroup> dataSourceGroupMap) {
		this.dataSourceGroupMap = dataSourceGroupMap;
		this.shardingDBCount = dataSourceGroupMap.size();
	}

	public List<ShardingTableInfo> getShardingTableInfos() {
		return shardingTableInfos;
	}

	public void setShardingTableInfos(List<ShardingTableInfo> shardingTableInfos) {
		this.shardingTableInfos = shardingTableInfos;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
