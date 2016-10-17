/*
 * Copyright (c) 2016-2020 LEJR.COM All Right Reserved
 */

package com.krzn.mybatis.spring.sharding.datasource;

import java.util.List;

import javax.sql.DataSource;

/**
 * 数据源负载策略。
 * 
 * @author WuHong
 * @version 1.0 
 * @date 2016年3月17日
 */
public interface LoadBalance {

	/**
	 * 从一组数据源中选择一个数据源。
	 * 
	 * @param dataSources 数据源组。
	 * @return
	 *      选择的数据源。
	 */
	DataSource select(List<DataSource> dataSources);
	
}
