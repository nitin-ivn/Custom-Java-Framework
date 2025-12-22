package io.nitin.di;

import io.nitin.annotations.Inject;
import io.nitin.annotations.Service;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Container {

    private Map<Class<?>, BeanDefinition> registry = new HashMap<>();
    private Map<Class<?>, Object> singletonCache = new HashMap<>();
    private Set<Class<?>> inCreation = new HashSet<>();



    public Container() throws Exception{
        registry = ComponentScanner.scan();

        for(BeanDefinition def : registry.values()){
            if(def.isSingleton()){
                getBean(def.getBeanClass());
            }
        }


    }

    private <T>T getBean(Class<T> clazz){
        BeanDefinition def = registry.get(clazz);

        if(def.isSingleton()){
            if(singletonCache.containsKey(clazz)){
                return clazz.cast(singletonCache.get(clazz));
            }
        }

        return createBean(clazz, def);
    }

    private <T> T createBean(Class<T> clazz, BeanDefinition def) {
        try{
            if(!def.isSingleton()){
                if(inCreation.contains(clazz)){
                    throw new Exception("Circular Dependency");
                }
                inCreation.add(clazz);
            }
            T instance = clazz.getDeclaredConstructor().newInstance();
            if(def.isSingleton()) singletonCache.put(clazz, instance);

            for(Field field : clazz.getDeclaredFields()){
                if(field.isAnnotationPresent(Inject.class)){
                    field.setAccessible(true);
                    Object dependency = getDependency(field.getType());
                    if(dependency == null){
                        System.out.println("Will be handled by custom exceptions later");
                        throw new Exception();
                    }
                    field.set(instance,dependency);
                }
            }
            inCreation.remove(clazz);

            return instance;
        } catch (Exception e){
            throw new RuntimeException("Failed to create bean: " + clazz.getName(), e);
        }
    }




    private Object getDependency(Class<?> type){
        BeanDefinition def = registry.get(type);
        if(def != null){
            return getBean(type);
        }

        for(Map.Entry<Class<?>, BeanDefinition> entry : registry.entrySet()){
            Class<?> clazz = entry.getKey();
            if(type.isAssignableFrom(clazz)){
                return getBean(clazz);
            }
        }

        return null;
    }

}