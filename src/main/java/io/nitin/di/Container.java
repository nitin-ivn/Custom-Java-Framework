package io.nitin.di;

import io.nitin.annotations.Service;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Container {
    private Map<Class<?>, Object> services = new HashMap<>();

    public Container(Class<?>... classes) throws Exception {
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Service.class)) {
                services.put(clazz, clazz.getDeclaredConstructor().newInstance());
            }
        }
//        for (Object service : services.values()) {
//            for (Field field : service.getClass().getDeclaredFields()) {
//                if (field.isAnnotationPresent(Inject.class)) {
//                    field.setAccessible(true);
//                    field.set(service, services.get(field.getType()));
//                }
//            }
//        }
    }

    public <T> T getService(Class<T> clazz) {
        return clazz.cast(services.get(clazz));
    }
}