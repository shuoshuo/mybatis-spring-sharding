package com.krzn.mybatis.spring.sharding.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.krzn.mybatis.spring.sharding.TableShardingBean;
import com.krzn.mybatis.spring.sharding.datasource.ShardingTableInfo;
import com.krzn.mybatis.spring.sharding.datasource.VirtualDataSource;


/**
 * 分表信息持有者
 *
 * @author yanhuajian
 * @date:2016年4月3日 下午9:13:09
 * @version V1.0
 *
 */
public class TableShardingHolder {

    /**
     * 使用private修饰，防止被new
     */
    private TableShardingHolder() {
    }

    /**
     * 分表元信息。
     * <p>
     * 这份数据配置在spring配置文件中，通过spring启动过程中 的Bean初始化回调机制来构建数据。
     */
    private static Map<String, TableShardingBean> tableShardingInfos = new HashMap<String, TableShardingBean>();

    /**
     * 初始化分表信息
     *
     * @param vdsMap
     * @author: yanhuajian 2016年4月3日下午9:07:36
     */
    public static void initTableShardingInfo(Map<String, VirtualDataSource> vdsMap) {
        Collection<VirtualDataSource> vdsCol = vdsMap.values();
        for (VirtualDataSource vds : vdsCol) {
            int shardingDBCount = vds.getShardingDBCount();
            List<ShardingTableInfo> stInfos = vds.getShardingTableInfos();
            for (ShardingTableInfo info : stInfos) {
                tableShardingInfos.put(info.getTablePrefix(),
                        new TableShardingBean(info.getTablePrefix(), vds.getName(),
                                info.getShardingTableCount(), shardingDBCount, info.getRealTableCount(),
                                info.getShardingStrategy()));
            }
        }
    }

    /**
     * 获取分表信息Map
     *
     * @return
     * @author: yanhuajian 2016年4月3日下午9:09:36
     */
    public static Map<String, TableShardingBean> getTableShardingInfos() {
        return tableShardingInfos;
    }

}
