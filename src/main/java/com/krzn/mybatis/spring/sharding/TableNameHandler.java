/*
 * Copyright (c) 2016-2020 LEJR.COM All Right Reserved
 */

package com.krzn.mybatis.spring.sharding;


/**
 * 分表表名处理器。
 * 
 * <p>注：这种表名一般是虚拟的表名，比如表名是user，实际的可能是user_0001
 * 
 * @author WuHong
 * @version 1.0 
 * @date 2016年3月16日
 */
public interface TableNameHandler {

	/**
	 * 生成实际的表名。
	 * 
	 * @param tablePrefix 表前缀。
	 * @param shardingTableNo 分表号。
	 * @param shardingTableCount 分表总数。
	 * @return
	 *      实际的物理表名。
	 */
	String generateRealTableName(String tablePrefix, int shardingTableNo, int shardingTableCount);
	
}
