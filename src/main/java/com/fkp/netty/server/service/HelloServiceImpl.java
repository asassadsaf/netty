package com.fkp.netty.server.service;

public class HelloServiceImpl implements HelloService{
    @Override
    public String sayHello(String name) {
        return "hello" + name;
    }
}
