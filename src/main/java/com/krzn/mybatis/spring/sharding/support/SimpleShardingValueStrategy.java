/*
 * Copyright (c) 2016-2020 LEJR.COM All Right Reserved
 */

package com.krzn.mybatis.spring.sharding.support;

import com.krzn.mybatis.spring.sharding.ShardingValueStrategy;

/**
 * ShardingValueStrategy的简单实现。
 * 
 * @author WuHong
 * @version 1.0 
 * @date 2016年3月18日
 */
public class SimpleShardingValueStrategy implements ShardingValueStrategy{

	public Object get(Object propertyValue) {
		return propertyValue;
	}

}
