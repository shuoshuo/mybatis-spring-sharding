/*
 * Copyright (c) 2016-2020 LEJR.COM All Right Reserved
 */

package com.krzn.mybatis.spring.sharding.support;

import com.krzn.mybatis.spring.sharding.ShardingDataSourceNameGenerator;

/**
 * 默认的Sharding数据源实例名称生成器实现。
 * 
 * @author WuHong
 * @version 1.0 
 * @date 2016年3月17日
 */
public class DefaultShardingDataSourceNameGenerator implements ShardingDataSourceNameGenerator{

	private String dataSourceNamePrefix = "dsg-";
	
	public String generate(int dataSourceNo, int shardingDBCount) {
		if(dataSourceNo < 0){
			throw new IllegalArgumentException("invalid dataSourceNo:" + dataSourceNo);
		}
		if(shardingDBCount <= 0){
			throw new IllegalArgumentException("invalid shardingDBCount:" + shardingDBCount);
		}
		if(shardingDBCount == 1){
			dataSourceNo = 0;
		}
		/*
		 * 数据源名称生成逻辑：
		 * 如果当前数据源号=1，分库总数为8，数据源名称前缀为DB_,
		 * 那么生成的数据源名称为DB_1。
		 * 如果当前数据源号=5，分库总数为16，数据源名称前缀为DB_,
		 * 那么生成的数据源名称为DB_05。
		 */
		int suffixWidth = (shardingDBCount + "").length();
		String tableSuffix = String.format("%0"+suffixWidth+"d", dataSourceNo); 
		StringBuilder builder = new StringBuilder(dataSourceNamePrefix);
		return builder.append(tableSuffix).toString();
	}

	public void setDataSourceNamePrefix(String dataSourceNamePrefix) {
		this.dataSourceNamePrefix = dataSourceNamePrefix;
	}
	
}
