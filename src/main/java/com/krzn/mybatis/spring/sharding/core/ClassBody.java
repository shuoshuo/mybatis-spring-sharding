package com.krzn.mybatis.spring.sharding.core;

import java.lang.reflect.Method;

/**
 * Java类体对象
 *
 * @author yangzhishuo
 * @date:2016年4月2日 下午7:09:19
 * @version V1.0
 *
 */
public class ClassBody {
    public Class<?> classObject;
    public Method method;
    public Class<?>[] paramTypes;

    public ClassBody() {
    }

    public ClassBody(Class<?> classObject, Method method, Class<?>[] paramTypes) {
        this.classObject = classObject;
        this.method = method;
        this.paramTypes = paramTypes;
    }

    public Class<?> getClassObject() {
        return classObject;
    }

    public void setClassObject(Class<?> classObject) {
        this.classObject = classObject;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Class<?>[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(Class<?>[] paramTypes) {
        this.paramTypes = paramTypes;
    }

}
