package com.krzn.mybatis.spring.sharding.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于支持分表的注解。
 * <p>这个注解用在Dao上，注解中需要指定分表前缀。
 *
 * <h2>tablePrefix：</h2>
 * <p>分表前缀指的是在sql中出现的(分)表名，这个是虚拟表名(或者逻辑表名)，
 * <p>比如：分表sql语句 "select * from user" 中表名是user，
 * 但实际的表名可能是user001。所以这里称之为<u>分表前缀</u>。
 * <p><b>注意：注解中的tablePrefix要和sql中的表名保持一致!</b>
 *
 * <p><b>注意：如果dao中的方法上的Sharding注解中指定了tablePrefix,
 * 那么优先使用方法上指定的表前缀，否则使用当前注解中指定的表前缀!</b>
 *
 * @author yangzhishuo
 * @version 1.0
 * @date 2016年3月28日
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ShardingTable {

    /**
     * 分表的表前缀，sql中一般直接将这个前缀作为逻辑表名。
     */
    String tablePrefix();

}
