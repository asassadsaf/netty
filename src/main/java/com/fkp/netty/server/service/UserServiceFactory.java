package com.fkp.netty.server.service;

public abstract class UserServiceFactory {

    private static final UserService USER_SERVICE = new UserServiceMemoryImpl();

    public static UserService getUserService(){
        return USER_SERVICE;
    }

}
