package io.nitin.di.contracts;

import io.nitin.di.BeanDefinition;

public interface BeanFactory {
    <T> T getBean(Class<T> clazz);
    <T> T createBean(Class<T> clazz, BeanDefinition def);
}
