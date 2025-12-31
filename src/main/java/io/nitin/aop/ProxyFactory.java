package io.nitin.aop;

import java.lang.reflect.Proxy;

public class ProxyFactory {
    @SuppressWarnings("unchecked")
    public static <T> T wrap(Object target, Class<T> interfaceType){
        return (T) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                new Class<?>[]{interfaceType},
                new LoggingInvocationHandler(target)
        );
    }
}
