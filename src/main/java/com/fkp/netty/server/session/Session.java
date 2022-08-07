package com.fkp.netty.server.session;

import io.netty.channel.Channel;

public interface Session {

    /**
     * 绑定会话
     * @param channel 哪个channel要绑定会话
     * @param username 会话绑定的用户
     */
    void bind(Channel channel, String username);

    /**
     * 解绑会话
     * @param channel 哪个channel要解绑会话
     */
    void unbind(Channel channel);

    /**
     * 根据用户名获取channel
     * @param username 用户名
     * @return 该用户名绑定的channel
     */
    Channel getChannel(String username);

    /**
     * 获取属性
     * @param channel 哪个channel
     * @param name 属性名
     * @return 属性值
     */
    Object getAttribute(Channel channel, String name) throws Exception;

    /**
     * 设置属性
     * @param channel 哪个channel
     * @param name 属性名
     * @param value 属性值
     */
    void setAttribute(Channel channel, String name, Object value) throws Exception;
}
