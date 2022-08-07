package com.fkp.netty.server.handler;

import com.fkp.netty.message.GroupChatRequestMessage;
import com.fkp.netty.message.GroupChatResponseMessage;
import com.fkp.netty.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

@ChannelHandler.Sharable
public class GroupChatRequestMessageHandler extends SimpleChannelInboundHandler<GroupChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupChatRequestMessage msg) throws Exception {
        String groupName = msg.getGroupName();
        List<Channel> channels = GroupSessionFactory.getGroupSession().getMembersChannel(groupName);
        for (Channel channel : channels) {
            channel.writeAndFlush(new GroupChatResponseMessage(msg.getFrom(),msg.getContent()));
        }
    }
}
