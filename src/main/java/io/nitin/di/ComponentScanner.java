package io.nitin.di;

import io.nitin.annotations.Scope;
import io.nitin.annotations.Service;
import io.nitin.enums.ScopeType;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ComponentScanner {
    private static final String packageName = "io.nitin.demo";

    public static Map<Class<?>,BeanDefinition> scan() {
        return scanPackage(packageName);
    }

    private static Map<Class<?>,BeanDefinition> scanPackage(String pkgName){
        Map<Class<?>,BeanDefinition> map = new HashMap<>();

        try{
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            String path = pkgName.replace('.','/');
            URL url = classLoader.getResource(path);

            if(url != null) {
                File directory = new File(url.toURI());
                File[] files = directory.listFiles();

                if(files == null) return map;
                for (File file : files) {
                    if (file.isDirectory()) {
                        map.putAll(scanPackage(pkgName + "." + file.getName()));
                    } else if (file.getName().endsWith(".class")) {
                        if (file.getName().contains("$")) continue;
                        String className = pkgName + '.' + file.getName().substring(0, file.getName().length() - 6);
                        Class<?> clazz = Class.forName(className);
                        if (clazz.isAnnotationPresent(Service.class)) {
                            boolean isSingleton = true;
                            if(clazz.isAnnotationPresent(Scope.class)){
                                Scope scope = clazz.getAnnotation(Scope.class);
                                if(scope.value() == ScopeType.PROTOTYPE){
                                    isSingleton = false;
                                }
                            }
                            map.put(clazz,new BeanDefinition(clazz, isSingleton));

                        }
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return map;
    }
}
