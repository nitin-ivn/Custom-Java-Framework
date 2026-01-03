package io.nitin.di;

import io.nitin.annotations.Inject;
import io.nitin.aop.AOPPostProcessor;
import io.nitin.di.Exceptions.BeanInitializationException;
import io.nitin.di.Exceptions.BeanNotFoundException;
import io.nitin.di.Exceptions.CircularDependencyException;
import io.nitin.di.contracts.BeanFactory;
import io.nitin.di.contracts.BeanPostProcessor;
import io.nitin.di.contracts.ObjectFactory;

import java.lang.reflect.Field;
import java.util.*;

public class Container implements BeanFactory {

    private final Map<Class<?>, BeanDefinition> registry;
    private final Map<Class<?>, Object> singletonCache = new HashMap<>();
    private final Map<Class<?>, Object> earlySingletonCache = new HashMap<>();
    private final Map<Class<?>, ObjectFactory<?>> singletonFactories = new HashMap<>();
    private final Set<Class<?>> inCreation = new HashSet<>();
    private final List<BeanPostProcessor> postProcessors = new ArrayList<>();

    public Map<?, BeanDefinition> getRegistry(){
        return registry;
    }


    public Container(){
        registry = ComponentScanner.scan();

        this.postProcessors.add(new AOPPostProcessor());

        for(BeanDefinition def : registry.values()){
            if(def.isSingleton()){
                getBean(def.getBeanClass());
            }
        }


    }

    public <T>T getBean(Class<T> clazz){
        if(singletonCache.containsKey(clazz)){
            return clazz.cast(singletonCache.get(clazz));
        }

        if(earlySingletonCache.containsKey(clazz)){
            return clazz.cast(earlySingletonCache.get(clazz));
        }

        if(singletonFactories.containsKey(clazz)){
            Object bean = singletonFactories.get(clazz).getObject();
            earlySingletonCache.put(clazz, bean);
            singletonFactories.remove(clazz);
            return clazz.cast(bean);
        }

        BeanDefinition def = registry.get(clazz);
        return createBean(clazz, def);
    }

    public <T> T createBean(Class<T> clazz, BeanDefinition def) {
        try{
            if(!def.isSingleton()){
                if(inCreation.contains(clazz)){
                    throw new CircularDependencyException("Circular dependency detected for: " + clazz.getName());
                }
                inCreation.add(clazz);
            }
            T instance = clazz.getDeclaredConstructor().newInstance();
            if(def.isSingleton()){
                final Object rawInstance = instance;
                singletonFactories.put(clazz, () -> getEarlyBeanReference(clazz,rawInstance));
            }

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

            Object bean = instance;

            if(earlySingletonCache.containsKey(clazz)){
                bean = earlySingletonCache.get(clazz);
            }else{
                for(BeanPostProcessor processor : postProcessors){
                    bean = processor.postProcessAfterInitialization(bean,clazz);
                }
            }

            if(def.isSingleton()){
                singletonCache.put(clazz,bean);
                earlySingletonCache.remove(clazz);
                singletonFactories.remove(clazz);
            }

            return (T) bean;
        } catch (NoSuchMethodException e) {
            throw new BeanInitializationException("No default constructor found for: " + clazz.getName());
        } catch (ReflectiveOperationException e) {
            throw new BeanInitializationException("Failed to instantiate: " + clazz.getName(), e);
        } catch (Exception e){
            throw new RuntimeException("Failed to create bean: " + clazz.getName(), e);
        } finally {
            inCreation.remove(clazz);
            singletonFactories.remove(clazz);
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

    private Object getEarlyBeanReference(Class<?> clazz, Object bean){
        Object obj = bean;
        for(BeanPostProcessor processor : postProcessors){
            obj = processor.postProcessAfterInitialization(obj,clazz);
        }
        return obj;
    }

}