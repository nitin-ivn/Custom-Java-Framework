package io.nitin.di;

import io.nitin.annotations.Service;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class ComponentScanner {
    private static final String packageName = "io.nitin.demo";

    public static Set<Class<?>> scan() {
        return scanPackage(packageName);
    }

    private static Set<Class<?>> scanPackage(String pkgName){
        Set<Class<?>> set = new HashSet<>();

        try{
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            String path = pkgName.replace('.','/');
            URL url = classLoader.getResource(path);

            if(url != null) {
                File directory = new File(url.toURI());
                File[] files = directory.listFiles();

                if(files == null) return set;
                for (File file : files) {
                    if (file.isDirectory()) {
                        set.addAll(scanPackage(pkgName + "." + file.getName()));
                    } else if (file.getName().endsWith(".class")) {
                        if (file.getName().contains("$")) continue;
                        String className = pkgName + '.' + file.getName().substring(0, file.getName().length() - 6);
                        Class<?> clazz = Class.forName(className);
                        if (clazz.isAnnotationPresent(Service.class)) {
                            set.add(clazz);
                        }
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return set;
    }
}
