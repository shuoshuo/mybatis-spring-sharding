package com.krzn.mybatis.spring.sharding.support;

/**
 * 动态取值策略
 *
 * @author yanhuajian
 * @date:2016年4月2日 下午1:58:49   
 * @version V1.0   
 *
 */
public interface DynamicValueStrategy {

	public Object get(String key);
}
