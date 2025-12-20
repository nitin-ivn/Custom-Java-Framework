package io.nitin.di;

import io.nitin.annotations.Inject;
import io.nitin.annotations.Service;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Container {
    private Map<Class<?>, Object> services = new HashMap<>();
    private Set<Class<?>> annotatedClasses;

    public Container() throws Exception {
        annotatedClasses = ComponentScanner.scan();
        for (Class<?> clazz : annotatedClasses) {
            if (clazz.isAnnotationPresent(Service.class)) {
                services.put(clazz, clazz.getDeclaredConstructor().newInstance());
            }
        }

        for (Object service : services.values()) {
            for (Field field : service.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    Object dependency = getDependency(field.getType());
                    if (dependency == null) {
                        System.out.println("Will be handled by custom exceptions later");
                        throw new Exception();
                    }
                    field.set(service, dependency);
                }
            }
        }
    }

    private Object getDependency(Class<?> type){
        if(services.containsKey(type)){
            return services.get(type);
        }

        for(Class<?> clazz : services.keySet()){
            if(type.isAssignableFrom(clazz)){
                return services.get(clazz);
            }
        }
        return null;
    }

    public <T> T getService(Class<T> clazz) {
        return clazz.cast(services.get(clazz));
    }
}