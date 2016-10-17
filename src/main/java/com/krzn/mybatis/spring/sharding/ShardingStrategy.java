/*
 * Copyright (c) 2016-2020 LEJR.COM All Right Reserved
 */

package com.krzn.mybatis.spring.sharding;

/**
 * 拆分(分库分表)策略。
 * 
 * @author WuHong
 * @version 1.0 
 * @date 2016年3月16日
 */
public interface ShardingStrategy {
	
	/**
	 * 根据sharding字段来确定分表号和分库号。
	 * 
	 * @param shardingValue sharding值。
	 * @param shardingTableCount 分表总数。
	 * @param shardingDBCount 分库总数。
	 * @return
	 *      包含分表号和分库号的元组。
	 */
	<V> NumberPair sharding(V shardingValue, int shardingTableCount, int shardingDBCount);

	
	
	
	public static class NumberPair{
		
		public static NumberPair pair(int tableNo, int dataSourceNo){
			NumberPair namePair = new NumberPair();
			namePair.tableNo = tableNo;
			namePair.dataSourceNo = dataSourceNo;
			return namePair;
		}
		
		private NumberPair(){}
		
		private int tableNo;
		private int dataSourceNo;
		public int getTableNo() {
			return tableNo;
		}

		public int getDataSourceNo() {
			return dataSourceNo;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + dataSourceNo;
			result = prime * result + tableNo;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NumberPair other = (NumberPair) obj;
			if (dataSourceNo != other.dataSourceNo)
				return false;
			if (tableNo != other.tableNo)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "NumberPair [tableNo=" + tableNo + ", dataBaseNo="
					+ dataSourceNo + "]";
		}
		
	}
	
}
