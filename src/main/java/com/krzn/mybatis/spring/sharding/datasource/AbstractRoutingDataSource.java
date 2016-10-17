/*
 * Copyright (c) 2016-2020 LEJR.COM All Right Reserved
 */

package com.krzn.mybatis.spring.sharding.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.lookup.DataSourceLookup;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

/**
 * spring的AbstractRoutingDataSource要支持层级，
 * 从数据源轮询的话还比较麻烦，自己改一下。
 * 
 * @author WuHong
 * @version 1.0 
 * @date 2016年3月18日
 * @see org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
 */
public abstract class AbstractRoutingDataSource extends AbstractDataSource{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRoutingDataSource.class);

	/**
	 * 数据源查找器。
	 */
	private DataSourceLookup dataSourceLookup;

	/**
	 * Set the DataSourceLookup implementation to use for resolving data source
	 * name Strings in the { targetDataSources} map.
	 * <p>Default is a {@link JndiDataSourceLookup}, allowing the JNDI names
	 * of application server DataSources to be specified directly.
	 */
	public void setDataSourceLookup(DataSourceLookup dataSourceLookup) {
		this.dataSourceLookup = (dataSourceLookup != null ? dataSourceLookup : new JndiDataSourceLookup());
	}

	public Connection getConnection() throws SQLException {
		return determineTargetDataSource().getConnection();
	}

	public Connection getConnection(String username, String password) throws SQLException {
		return determineTargetDataSource().getConnection(username, password);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if (iface.isInstance(this)) {
			return (T) this;
		}
		return determineTargetDataSource().unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return (iface.isInstance(this) || determineTargetDataSource().isWrapperFor(iface));
	}

	/**
	 * Retrieve the current target DataSource. Determines the
	 * {@link #determineCurrentLookupKey() current lookup key}, performs
	 * a lookup in the map,
	 * falls back to the specified
	 *  if necessary.
	 * @see #determineCurrentLookupKey()
	 */
	protected DataSource determineTargetDataSource() {
		String lookupKey = determineCurrentLookupKey();
		DataSource dataSource = this.dataSourceLookup.getDataSource(lookupKey);
		if (dataSource == null) {
			throw new IllegalStateException("无法找到目标查找key=[" + lookupKey + "]对应的数据源!");
		}
		try {
			if(LOGGER.isDebugEnabled()){
				Connection connection = null;
				try{
					connection = dataSource.getConnection();
					String dburl = connection.getMetaData().getURL();
					LOGGER.debug("已经选择的数据源信息:[DBURL="+dburl+"]");
				}finally{
					if(connection != null){
						connection.close();
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return dataSource;
	}

	/**
	 * Determine the current lookup key. This will typically be
	 * implemented to check a thread-bound transaction context.
	 * <p>Allows for arbitrary keys. The returned key needs
	 * to match the stored lookup key type, as resolved by the
	 * method.
	 */
	protected abstract String determineCurrentLookupKey();
	
}
