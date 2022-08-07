package com.fkp.netty.server.session;

import io.netty.channel.Channel;

import java.util.List;
import java.util.Set;

public interface GroupSession {

    /**
     * 创建一个聊天组，如果不存在才能创建，即聊天组的name不能重复，否则返回组名为name的组对象
     * @param name 聊天组名
     * @param members 成员
     * @return 成功返回null，若组名已存在导致失败则返回该组对象
     */
    Group createGroup(String name, Set<String> members);

    /**
     * 加入聊天组
     * @param name 组名
     * @param member 成员名
     * @return 如果组不存在则返回null，否则返回组对象
     */
    Group joinMember(String name, String member);

    /**
     * 移除组成员
     * @param name 组名
     * @param member 成员名
     * @return 如果组不存在返回null，否则返回组对象
     */
    Group removeMember(String name, String member);

    /**
     * 获取组成员
     * @param name 组名
     * @return 成员集合，没有成员返回empty set
     */
    Set<String> getMembers(String name);

    /**
     * 获取组成员的channel集合，只有在线的channel才返回
     * @param name 组名
     * @return 组成员的channel集合
     */
    List<Channel> getMembersChannel(String name);

    /**
     * 移除聊天组
     * @param name 组名
     * @return 如果组不存在返回 null, 否则返回组对象
     */
    Group removeGroup(String name);
}
