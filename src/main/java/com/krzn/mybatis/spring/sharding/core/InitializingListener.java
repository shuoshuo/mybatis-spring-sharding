package com.krzn.mybatis.spring.sharding.core;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.krzn.mybatis.spring.sharding.datasource.VirtualDataSource;
/**
 * 系统初始化监听器
 *
 * @author yangzhishuo
 * @date:2016年4月3日 下午9:01:02
 * @version V1.0
 *
 */
public class InitializingListener implements InitializingBean, ApplicationContextAware {

    /**
     * spring上下文。
     */
    private ApplicationContext springContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        // 从spring上下文中拿出一些分表元信息。
        Map<String, VirtualDataSource> vdsMap = this.springContext.getBeansOfType(VirtualDataSource.class);
        // 初始化分表信息
        TableShardingHolder.initTableShardingInfo(vdsMap);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.springContext = applicationContext;
    }

}
