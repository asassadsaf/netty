package com.fkp.netty.config;

import com.fkp.netty.protocol.Serializer;

import java.io.InputStream;
import java.util.Properties;

public class Config {

    private static final Properties properties;

    static {
        try(InputStream in = Config.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties = new Properties();
            properties.load(in);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Serializer.Algorithm getSerializerAlgorithm(){
        String alg = properties.getProperty("serializer.algorithm");
        if(alg == null){
            return Serializer.Algorithm.Java;
        }else {
            return Serializer.Algorithm.valueOf(alg);
        }
    }

    public static int getServerPort(){
        String port = properties.getProperty("server.port");
        if(port == null){
            return 8080;
        }else {
            return Integer.parseInt(port);
        }
    }
}
