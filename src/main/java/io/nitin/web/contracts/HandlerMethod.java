package io.nitin.web.contracts;

import java.lang.reflect.Method;

public class HandlerMethod {
    private final Object bean;
    private final Method method;

    public HandlerMethod(Object bean, Method method){
        this.bean = bean;
        this.method = method;
    }

    public Object invoke(Object... args) throws Exception{
        return method.invoke(bean,args);
    }
}
