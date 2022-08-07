package com.fkp.netty.server.session;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionMemoryImpl implements Session{

    private final Map<String, Channel> usernameChannelMap = new ConcurrentHashMap<>();
    private final Map<Channel, String> channelUsernameMap = new ConcurrentHashMap<>();
    private final Map<Channel, Map<String, Object>> channelAttributesMap = new ConcurrentHashMap<>();

    @Override
    public void bind(Channel channel, String username) {
        usernameChannelMap.put(username,channel);
        channelUsernameMap.put(channel,username);
        channelAttributesMap.put(channel,new ConcurrentHashMap<>());
    }

    @Override
    public void unbind(Channel channel) {
        usernameChannelMap.remove(channelUsernameMap.remove(channel));
        channelAttributesMap.remove(channel);
    }

    @Override
    public Channel getChannel(String username) {
        return usernameChannelMap.get(username);
    }

    @Override
    public Object getAttribute(Channel channel, String name) throws Exception {
        Map<String, Object> map = channelAttributesMap.get(channel);
        if(map == null){
            throw new Exception("channel may not bind to user");
        }
        return map.get(name);
    }

    @Override
    public void setAttribute(Channel channel, String name, Object value) throws Exception {
        Map<String, Object> map = channelAttributesMap.get(channel);
        if(map == null){
            throw new Exception("channel may not bind to user");
        }
        map.put(name,value);
    }
}
