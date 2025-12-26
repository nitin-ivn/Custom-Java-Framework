package io.nitin.di;

import io.nitin.annotations.Inject;
import io.nitin.di.Exceptions.BeanInitializationException;
import io.nitin.di.Exceptions.BeanNotFoundException;
import io.nitin.di.Exceptions.CircularDependencyException;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Container {

    private final Map<Class<?>, BeanDefinition> registry;
    private final Map<Class<?>, Object> singletonCache = new HashMap<>();
    private final Set<Class<?>> inCreation = new HashSet<>();



    public Container(){
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
                    throw new CircularDependencyException("Circular dependency detected for: " + clazz.getName());
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
                        throw new BeanNotFoundException("Class or Subclass not Found for the: " + clazz.getName());
                    }
                    field.set(instance,dependency);
                }
            }
            return instance;
        } catch (NoSuchMethodException e) {
            throw new BeanInitializationException("No default constructor found for: " + clazz.getName());
        } catch (ReflectiveOperationException e) {
            throw new BeanInitializationException("Failed to instantiate: " + clazz.getName(), e);
        } catch (Exception e){
            throw new RuntimeException("Failed to create bean: " + clazz.getName(), e);
        } finally {
            inCreation.remove(clazz);
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