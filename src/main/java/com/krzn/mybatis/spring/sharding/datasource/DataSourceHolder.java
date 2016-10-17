package com.krzn.mybatis.spring.sharding.datasource;

/**
 * 调查令牌，绑定到当前的线程，传到数据源路由器，进行分库判断
 * 
 * @author yanhuajian
 * @date:2016年3月29日 下午1:14:41
 * @version V1.0
 * 
 */
public class DataSourceHolder {

	/**
	 * 用于存放当前使用的数据源号
	 */
	private static final ThreadLocal<Integer> CURRENT_DS_HOLDER = new ThreadLocal<Integer>();
	/**
	 * 用于存放当前使用的数据库表号
	 */
	private static final ThreadLocal<Integer> CURRENT_TB_HOLDER = new ThreadLocal<Integer>();
	
	/**
	 * 用于存放真实的数据库表号
	 */
	private static final ThreadLocal<String> REAL_TB_INDEX_HOLDER = new ThreadLocal<String>();

	/**
	 * 指定一个表号绑定到当前线程。<br>
	 * 设置这个值可以不依赖与分库规则，而是手工指定要执行的一个数据库表。<br>
	 * 比如你要操作一个表，而你没有那个表用于分库分表的字段值，那么你就可以设置这个值手工指定，尤其适用于全表扫描任务。
	 */
	public static void bindTableIndex(Integer tableIndex) {
		CURRENT_TB_HOLDER.set(tableIndex);
	}

	/**
	 * 从当前线程获得绑定的数据库表号
	 */
	public static Integer getTableIndex() {
		return CURRENT_TB_HOLDER.get();
	}

	/**
	 * 解除绑定的数据库表号
	 */
	public static void unbindTableIndex() {
		CURRENT_TB_HOLDER.remove();
	}
	
	/**
	 * 将数据源对象绑定到当前线程
	 */
	public static void bindDataSource(Integer dataSource) {
		CURRENT_DS_HOLDER.set(dataSource);
	}

	/**
	 * 从当前线程获得绑定的数据源
	 */
	public static Integer getDataSource() {
		return CURRENT_DS_HOLDER.get();
	}

	/**
	 * 解除绑定的数据源
	 */
	public static void unbindDataSource() {
		CURRENT_DS_HOLDER.remove();
	}
	
	/**
	 * 将真实的表号对象绑定到当前线程
	 */
	public static void bindRealTableIndex(String realTableIndex) {
		REAL_TB_INDEX_HOLDER.set(realTableIndex);
	}

	/**
	 * 从当前线程获得绑定的真实的表号
	 */
	public static String getRealTableIndex() {
		return REAL_TB_INDEX_HOLDER.get();
	}

	/**
	 * 解除绑定的真实的表号
	 */
	public static void unbindRealTableIndex() {
		REAL_TB_INDEX_HOLDER.remove();
	}

}
