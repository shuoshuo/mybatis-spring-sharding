package com.krzn.mybatis.spring.sharding.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.krzn.mybatis.spring.sharding.RW;

/**
 * 用于支持非分库分表，但需要读写分离和手工指定数据源的注解。
 * <p>这个注解用在Dao的方法上，注解中可以指定读写标志和手工数据源组key。
 *
 * <h2>rw：</h2>
 * <p>读写分离标示，写操作会命中主库，读操作会命中从库。
 *
 * <h2>manualDataSourceGroupKey：</h2>
 * <p>用于手工指定数据源组。
 *
 * @author 杨志硕
 * @version 1.0
 * @date 2016年3月28日
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface NonSharding {

    /**
     * 读写标示。
     */
    RW rw() default RW.NS;

    /**
     * 手工指定的数据源组key。
     */
    String manualDataSourceGroupKey() default "";

}
