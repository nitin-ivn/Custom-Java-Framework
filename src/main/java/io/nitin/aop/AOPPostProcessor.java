package io.nitin.aop;

import io.nitin.aop.annotations.Log;
import io.nitin.di.contracts.BeanPostProcessor;

public class AOPPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, Class<?> beanClass){
        if(beanClass.isAnnotationPresent(Log.class)){
            Class<?>[] interfaces = beanClass.getInterfaces();
            if(interfaces.length > 0){
                return ProxyFactory.wrap(bean, interfaces[0]);
            }
        }

        return bean;
    }
}
