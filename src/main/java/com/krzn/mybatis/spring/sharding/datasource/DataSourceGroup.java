/*
 * Copyright (c) 2016-2020 LEJR.COM All Right Reserved
 */

package com.krzn.mybatis.spring.sharding.datasource;

import java.util.List;

import javax.sql.DataSource;

/**
 * 数据源组。
 * <p>支持一主一从，一主多从的数据源组。
 * 
 * @author WuHong
 * @version 1.0 
 * @date 2016年3月17日
 */
public class DataSourceGroup {
	
	/**
	 * 名称。
	 */
	private String name;
	
	/**
	 * 主数据源。
	 */
	private DataSource masterDataSource;
	
	/**
	 * 从数据源。
	 */
	private List<DataSource> slaveDataSources;
	
	/**
	 * 用于从数据源的选取，默认为随机选取。
	 */
	private LoadBalance loadBalance = new RandomLoadBalance();

	public DataSource getMasterDataSource() {
		return masterDataSource;
	}

	public void setMasterDataSource(DataSource masterDataSource) {
		this.masterDataSource = masterDataSource;
	}

	public List<DataSource> getSlaveDataSources() {
		return slaveDataSources;
	}

	public void setSlaveDataSources(List<DataSource> slaveDataSources) {
		this.slaveDataSources = slaveDataSources;
	}

	public LoadBalance getLoadBalance() {
		return loadBalance;
	}

	public void setLoadBalance(LoadBalance loadBalance) {
		this.loadBalance = loadBalance;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
