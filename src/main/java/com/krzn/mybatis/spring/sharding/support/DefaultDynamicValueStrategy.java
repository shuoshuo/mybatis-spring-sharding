package com.krzn.mybatis.spring.sharding.support;

import com.krzn.mybatis.spring.sharding.datasource.DataSourceHolder;

/**
 * 默认的动态取值策略
 *
 * @author yanhuajian
 * @date:2016年4月2日 下午1:58:49   
 * @version V1.0   
 *
 */
public class DefaultDynamicValueStrategy implements DynamicValueStrategy {

	@Override
	public Object get(String key) {
		return DataSourceHolder.getRealTableIndex();
	}
}
