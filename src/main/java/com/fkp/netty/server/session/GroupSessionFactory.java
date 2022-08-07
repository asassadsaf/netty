package com.fkp.netty.server.session;

public abstract class GroupSessionFactory {

    private static final GroupSession session = new GroupSessionMemoryImpl();

    public static GroupSession getGroupSession() {
        return session;
    }
}
