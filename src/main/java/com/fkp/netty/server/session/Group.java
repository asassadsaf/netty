package com.fkp.netty.server.session;

import lombok.Data;

import java.util.Collections;
import java.util.Set;

@Data
/*
  聊天组，即聊天室
 */
public class Group {
    // 聊天室名称
    private String name;
    // 聊天室成员
    private Set<String> members;

    private static final Group EMPTY_GROUP = new Group("empty", Collections.emptySet());

    public Group(String name, Set<String> members) {
        this.name = name;
        this.members = members;
    }

    public static Group getGroup(){
        return EMPTY_GROUP;
    }
}
