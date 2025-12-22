package io.nitin.di;

public class BeanDefinition {
    public Class<?> getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public boolean isSingleton() {
        return isSingleton;
    }

    public void setSingleton(boolean singleton) {
        isSingleton = singleton;
    }

    public BeanDefinition(Class<?> beanClass, boolean isSingleton) {
        this.beanClass = beanClass;
        this.isSingleton = isSingleton;
    }

    private  Class<?> beanClass;
    private boolean isSingleton;


}
