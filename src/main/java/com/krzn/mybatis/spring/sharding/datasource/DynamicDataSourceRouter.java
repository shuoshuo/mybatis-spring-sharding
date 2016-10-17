package com.krzn.mybatis.spring.sharding.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 动态数据源路由器
 * 
 * @author yanhuajian
 * @date:2016年3月29日 下午1:05:52
 * @version V1.0
 * 
 */
public class DynamicDataSourceRouter extends AbstractRoutingDataSource {
	
	private static final String DS = "ds";

	@Override
	protected Object determineCurrentLookupKey() {
		// 判断数据源枚举的值，选择数据源
		Integer dsIndex = DataSourceHolder.getDataSource();
		if (dsIndex != null) {
			// 解除绑定
//			DataSourceHolder.unbindDataSource();

			return DS + dsIndex;
		}

		// 返回null，则走默认配置的数据源
		return null;
	}

}
