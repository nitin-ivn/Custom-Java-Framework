package io.nitin.web;

import io.nitin.di.BeanDefinition;
import io.nitin.di.Container;
import io.nitin.web.annotations.GetMapping;
import io.nitin.web.annotations.RestController;
import io.nitin.web.contracts.HandlerMethod;
import jakarta.servlet.http.HttpServlet;

import java.lang.reflect.Method;
import java.util.HashMap;

public class DispatcherServlet extends HttpServlet {
    private final HashMap<String, HandlerMethod> handlerMap = new HashMap<>();
    private Container container;

    @Override
    public void init(){
        this.container  = new Container();

        for(BeanDefinition def : container.getRegistry().values()){
            Class<?> beanClass = def.getBeanClass();

            if(beanClass.isAnnotationPresent(RestController.class)){
                for(Method method : beanClass.getDeclaredMethods()){
                    if(method.isAnnotationPresent(GetMapping.class)){
                        GetMapping getMapping = method.getAnnotation(GetMapping.class);
                        String path = getMapping.value();

                        Object beanInstance = container.getBean(beanClass);
                        handlerMap.put("GET: " + path, new HandlerMethod(beanInstance, method));
                    }
                }
            }
        }
    }
}
