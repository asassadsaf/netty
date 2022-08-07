package com.fkp.netty.server.service;

public interface UserService {

    /**
     * 登录
     * @param username 用户名
     * @param password 密码
     * @return 登录成功返回true，失败返回false
     */
    boolean login(String username, String password);
}
