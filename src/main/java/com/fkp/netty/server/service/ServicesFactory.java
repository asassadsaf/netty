package com.fkp.netty.server.service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class ServicesFactory {
    private static Properties properties;
    private static Map<Class<?>,Object> map = new ConcurrentHashMap<>();
    static {
        try (InputStream in = ServicesFactory.class.getClassLoader().getResourceAsStream("application.properties")){
            properties = new Properties();
            properties.load(in);

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
