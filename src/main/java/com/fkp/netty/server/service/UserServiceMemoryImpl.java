package com.fkp.netty.server.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserServiceMemoryImpl implements UserService{

    private final Map<String, String> allUserMap = new ConcurrentHashMap<>();

    //初始化块，在对象创建时执行，在构造方法前执行
    {
        allUserMap.put("zhangsan","123");
        allUserMap.put("lisi","123");
        allUserMap.put("wangwu","123");
        allUserMap.put("zhaoliu","123");
        allUserMap.put("qianqi","123");
    }

    @Override
    public boolean login(String username, String password) {
        String pwd = allUserMap.get(username);
        if(pwd == null){
            return false;
        }
        return pwd.equals(password);
    }
}
