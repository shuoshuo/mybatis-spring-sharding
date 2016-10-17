/*
 * Copyright (c) 2016-2020 LEJR.COM All Right Reserved
 */

package com.krzn.mybatis.spring.sharding.support;

import com.krzn.mybatis.spring.sharding.ShardingStrategy;

/**
 * 默认的拆分策略实现。
 * <p>具体策略：
 * <p>通过字段值算出hashCode，然后对分表总数取模作为分表号。
 * 分表号除以分库总数作为分库号。
 * 
 * @author WuHong
 * @version 1.0 
 * @date 2016年3月17日
 */
public class DefaultShardingStrategy implements ShardingStrategy{

	public <V> NumberPair sharding(V shardingValue,
			int shardingTableCount, int shardingDBCount) {
		if(shardingValue == null){
			throw new IllegalArgumentException("invalid shardingColumnValue:" + shardingValue);
		}
		if(shardingTableCount <= 0){
			throw new IllegalArgumentException("invalid shardingTableCount:" + shardingTableCount);
		}
		if(shardingDBCount <= 0){
			throw new IllegalArgumentException("invalid shardingDBCount:" + shardingDBCount);
		}
		if(shardingTableCount < shardingDBCount){
			throw new IllegalArgumentException("shardingDBCount can't less than shardingTableCount");
		}
		
		int hash = shardingValue.toString().hashCode();
		if(hash < 0){
			hash = Math.abs(hash);
		}
		int shardingTableNo = hash % shardingTableCount;
		int shardingDBNo = shardingTableNo / (shardingTableCount / shardingDBCount);
		return NumberPair.pair(shardingTableNo, shardingDBNo);
	}

}
