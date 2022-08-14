package com.fkp.netty.server.service;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServicesFactory {
    private static final Properties properties;
    private static final Map<Class<?>,Object> map = new ConcurrentHashMap<>();
    static {
        try (InputStream in = ServicesFactory.class.getClassLoader().getResourceAsStream("application.properties")){
            properties = new Properties();
            properties.load(in);
            Set<String> names = properties.stringPropertyNames();
            for (String name : names) {
                if(name.endsWith("Service")){
                    map.put(Class.forName(name),Class.forName(properties.getProperty(name)).newInstance());
                }
            }
        }catch (Exception e){
            throw new ExceptionInInitializerError(e);
        }
    }

    public static <T> T getService(Class<T> clazz){
        return (T) map.get(clazz);
    }

    public static void main(String[] args) {
        HelloService service = ServicesFactory.getService(HelloService.class);
        System.out.println(service.getClass());
    }
}
