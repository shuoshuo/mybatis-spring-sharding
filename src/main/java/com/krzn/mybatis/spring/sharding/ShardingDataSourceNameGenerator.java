/*
 * Copyright (c) 2016-2020 LEJR.COM All Right Reserved
 */

package com.krzn.mybatis.spring.sharding;

/**
 * Sharding数据源实例名称生成器。
 * 
 * @author WuHong
 * @version 1.0 
 * @date 2016年3月17日
 */
public interface ShardingDataSourceNameGenerator {

	/**
	 * 根据数据源号生成数据源名称。
	 * 
	 * @param dataSourceNo 数据源号。
	 * @param shardingDBCount 分库总数。 
	 * @return
	 *      生成的数据源名称。
	 */
	String generate(int dataSourceNo, int shardingDBCount);
	
}
