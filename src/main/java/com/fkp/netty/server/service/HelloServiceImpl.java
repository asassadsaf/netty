package com.fkp.netty.server.service;

public class HelloServiceImpl implements HelloService{
    @Override
    public String sayHello(String name) {
//        int i = 1/0;
        return "hello" + name;
    }
}
