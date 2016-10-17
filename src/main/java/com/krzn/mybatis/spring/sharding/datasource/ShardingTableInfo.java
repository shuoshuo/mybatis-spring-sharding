/*
 * Copyright (c) 2016-2020 LEJR.COM All Right Reserved
 */

package com.krzn.mybatis.spring.sharding.datasource;

import com.krzn.mybatis.spring.sharding.ShardingStrategy;

/**
 * 分表信息。
 * 
 * @author WuHong
 * @version 1.0 
 * @date 2016年3月18日
 */
public class ShardingTableInfo {

	/**
	 * 数据表前缀。
	 */
	private String tablePrefix;
	
	/**
	 * 分表总数。
	 */
	private int shardingTableCount;
	
	/**
	 * 分表总数。
	 */
	private int realTableCount;
	
	/**
	 * 分库分表策略。
	 */
	private ShardingStrategy shardingStrategy;

	public String getTablePrefix() {
		return tablePrefix;
	}

	public void setTablePrefix(String tablePrefix) {
		this.tablePrefix = tablePrefix;
	}

	public int getShardingTableCount() {
		return shardingTableCount;
	}

	public void setShardingTableCount(int shardingTableCount) {
		this.shardingTableCount = shardingTableCount;
	}

	public int getRealTableCount() {
		if (this.realTableCount == 0) {
			return this.shardingTableCount;
		}
		return realTableCount;
	}

	public void setRealTableCount(int realTableCount) {
		this.realTableCount = realTableCount;
	}

	public ShardingStrategy getShardingStrategy() {
		return shardingStrategy;
	}

	public void setShardingStrategy(ShardingStrategy shardingStrategy) {
		this.shardingStrategy = shardingStrategy;
	}

	
	
}
