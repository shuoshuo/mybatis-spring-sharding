/*
 * Copyright (c) 2016-2020 LEJR.COM All Right Reserved
 */

package com.krzn.mybatis.spring.sharding.datasource;

/**
 * 支持分库分表、读写分离数据源。
 * 
 * @author WuHong
 * @version 1.0 
 * @date 2016年3月17日
 */
public class ShardingDataSource extends AbstractRoutingDataSource{
	
	@Override
	protected String determineCurrentLookupKey() {
		return DataSourceLocalKeys.CURRENT_VDS_KEY.get();
	}

}
