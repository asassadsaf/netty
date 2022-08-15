package com.fkp.netty.protocol;


import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;

public interface Serializer {

    //序列化方法
    <T> byte[] serializer(T object);

    //反序列化方法
    <T> T deserializer(Class<T> clazz, byte[] bytes);

    enum Algorithm implements Serializer{
        Java{
            @Override
            public <T> byte[] serializer(T object) {
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
                    oos.writeObject(object);
                    return bos.toByteArray();
                }catch (Exception e){
                    throw new RuntimeException("serializer fail",e);
                }
            }

            @Override
            public <T> T deserializer(Class<T> clazz, byte[] bytes) {
                try {
                    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    return (T) ois.readObject();
                }catch (Exception e){
                    throw new RuntimeException("deserializer fail",e);
                }
            }
        },
        Json{
            @Override
            public <T> byte[] serializer(T object) {
                return JSON.toJSONString(object).getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public <T> T deserializer(Class<T> clazz, byte[] bytes) {
                return JSON.parseObject(new String(bytes,StandardCharsets.UTF_8),clazz);
            }
        },
        Jackson{
            @Override
            public <T> byte[] serializer(T object) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    return objectMapper.writeValueAsString(object).getBytes(StandardCharsets.UTF_8);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("serializer fail",e);
                }
            }

            @Override
            public <T> T deserializer(Class<T> clazz, byte[] bytes) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    return objectMapper.readValue(new String(bytes, StandardCharsets.UTF_8),clazz);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("deserializer fail",e);
                }
            }
        }
    }
}
