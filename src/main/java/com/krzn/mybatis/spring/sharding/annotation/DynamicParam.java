package com.krzn.mybatis.spring.sharding.annotation;

import com.krzn.mybatis.spring.sharding.support.DefaultDynamicValueStrategy;
import com.krzn.mybatis.spring.sharding.support.DynamicValueStrategy;

/**
 * 动态参数注解
 *
 * @author yangzhishuo
 * @date:2016年4月2日 上午11:35:38
 * @version V1.0
 *
 */
public @interface DynamicParam {

    // 参数的key
    String key();
    // 参数的value值
    Class<? extends DynamicValueStrategy> value() default DefaultDynamicValueStrategy.class;
}
