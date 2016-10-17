/*
 * Copyright (c) 2016-2020 LEJR.COM All Right Reserved
 */

package com.krzn.mybatis.spring.sharding;

/**
 * 分表信息Bean。
 * <p>注：不同的虚拟数据源中不能出现相同的表前缀。
 * 
 * @author WuHong
 * @version 1.0 
 * @date 2016年3月18日
 */
public class TableShardingBean {

	/**
	 * 表前缀。
	 */
	private String tablePrefix;
	
	/**
	 * 虚拟数据源名称。
	 */
	private String vdsName;
	
	/**
	 * 分表总数。
	 */
	private int shardingTableCount;
	
	/**
	 * 真实分表总数
	 */
	private int realTableCount;
	
	/**
	 * 分库总数。
	 */
	private int shardingDBCount;
	
	/**
	 * 分库分表策略。
	 */
	private ShardingStrategy shardingStrategy;
	
	public TableShardingBean(String tablePrefix, String vdsName,
			int shardingTableCount, int shardingDBCount, int realTableCount, ShardingStrategy shardingStrategy) {
		super();
		this.tablePrefix = tablePrefix;
		this.vdsName = vdsName;
		this.shardingTableCount = shardingTableCount;
		this.shardingDBCount = shardingDBCount;
		this.realTableCount = realTableCount;
		this.shardingStrategy = shardingStrategy;
	}

	public String getTablePrefix() {
		return tablePrefix;
	}

	public void setTablePrefix(String tablePrefix) {
		this.tablePrefix = tablePrefix;
	}

	public int getRealTableCount() {
		return realTableCount;
	}

	public void setRealTableCount(int realTableCount) {
		this.realTableCount = realTableCount;
	}

	public int getShardingTableCount() {
		return shardingTableCount;
	}

	public void setShardingTableCount(int shardingTableCount) {
		this.shardingTableCount = shardingTableCount;
	}

	public int getShardingDBCount() {
		return shardingDBCount;
	}

	public void setShardingDBCount(int shardingDBCount) {
		this.shardingDBCount = shardingDBCount;
	}

	public String getVdsName() {
		return vdsName;
	}

	public void setVdsName(String vdsName) {
		this.vdsName = vdsName;
	}

	public ShardingStrategy getShardingStrategy() {
		return shardingStrategy;
	}

	public void setShardingStrategy(ShardingStrategy shardingStrategy) {
		this.shardingStrategy = shardingStrategy;
	}

	@Override
	public String toString() {
		return "TableShardingBean [tablePrefix=" + tablePrefix + ", vdsName=" + vdsName + ", shardingTableCount=" + shardingTableCount + ", shardingDBCount="
				+ shardingDBCount + ", shardingStrategy=" + shardingStrategy + "]";
	}
	
}
