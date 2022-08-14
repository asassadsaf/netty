package com.fkp.netty.rpc;

import com.fkp.netty.server.service.HelloService;

public class RpcClientManagerTest {
    public static void main(String[] args) {
        HelloService service = RpcClientManager.getProxyService(HelloService.class);
        String res = service.sayHello("张三");
        System.out.println(res);
    }
}
