package com.fkp.netty.server.handler;

import com.fkp.netty.message.GroupMembersRequestMessage;
import com.fkp.netty.message.GroupMembersResponseMessage;
import com.fkp.netty.server.session.GroupSessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Set;

@ChannelHandler.Sharable
public class GroupMembersRequestMessageHandler extends SimpleChannelInboundHandler<GroupMembersRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupMembersRequestMessage msg) throws Exception {
        String groupName = msg.getGroupName();
        Set<String> members = GroupSessionFactory.getGroupSession().getMembers(groupName);
        ctx.writeAndFlush(new GroupMembersResponseMessage(members));
    }
}
