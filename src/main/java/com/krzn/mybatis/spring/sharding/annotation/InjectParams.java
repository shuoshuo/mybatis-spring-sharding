package com.krzn.mybatis.spring.sharding.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 动态给Mabatis注入参数的注解
 *
 * @author yangzhishuo
 * @date:2016年4月1日 上午11:30:56
 * @version V1.0
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface InjectParams {
    // 参数集合
    DynamicParam[] params();
}
