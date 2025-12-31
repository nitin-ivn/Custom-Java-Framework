package io.nitin.di.contracts;

public interface BeanPostProcessor {
    Object postProcessAfterInitialization(Object bean, Class<?> beanClass);
}
