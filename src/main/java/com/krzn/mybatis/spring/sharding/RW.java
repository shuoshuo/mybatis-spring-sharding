/*
 * Copyright (c) 2016-2020 LEJR.COM All Right Reserved
 */

package com.krzn.mybatis.spring.sharding;

/**
 * 表示读/写的枚举。
 * 
 * @author WuHong
 * @version 1.0 
 * @date 2016年3月18日
 */
public enum RW {

	/**
	 * 读
	 */
	READ,
	
	/**
	 * 写
	 */
	WRITE,
	
	/**
	 * 未指定
	 */
	NS;
	
}
