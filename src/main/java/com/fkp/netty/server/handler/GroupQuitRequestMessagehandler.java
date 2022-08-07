package com.fkp.netty.server.handler;

import com.fkp.netty.message.GroupQuitRequestMessage;
import com.fkp.netty.message.GroupQuitResponseMessage;
import com.fkp.netty.server.session.Group;
import com.fkp.netty.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

@ChannelHandler.Sharable
public class GroupQuitRequestMessagehandler extends SimpleChannelInboundHandler<GroupQuitRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupQuitRequestMessage msg) throws Exception {
        String groupName = msg.getGroupName();
        String username = msg.getUsername();
        Group group = GroupSessionFactory.getGroupSession().removeMember(groupName, username);
        if(group == null){
            ctx.writeAndFlush(new GroupQuitResponseMessage(false,"group is not exist."));
        }else {
            ctx.writeAndFlush(new GroupQuitResponseMessage(true,"您已退出" + groupName + "群聊。"));
            List<Channel> channels = GroupSessionFactory.getGroupSession().getMembersChannel(groupName);
            for (Channel channel : channels) {
                channel.writeAndFlush(new GroupQuitResponseMessage(true,username + "退出" + groupName + "群聊。"));
            }
        }
    }
}
