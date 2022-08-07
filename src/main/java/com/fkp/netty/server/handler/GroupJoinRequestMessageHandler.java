package com.fkp.netty.server.handler;

import com.fkp.netty.message.GroupJoinRequestMessage;
import com.fkp.netty.message.GroupJoinResponseMessage;
import com.fkp.netty.server.session.Group;
import com.fkp.netty.server.session.GroupSessionFactory;
import com.fkp.netty.server.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

@ChannelHandler.Sharable
public class GroupJoinRequestMessageHandler extends SimpleChannelInboundHandler<GroupJoinRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupJoinRequestMessage msg) throws Exception {
        String groupName = msg.getGroupName();
        String username = msg.getUsername();
        Group group = GroupSessionFactory.getGroupSession().joinMember(groupName, username);
        if(group == null){
            //组不存在
            ctx.writeAndFlush(new GroupJoinResponseMessage(false,"group is not exist."));
        }else {
            //添加成员成功，并通知该成员和组内其他成员
            List<Channel> channels = GroupSessionFactory.getGroupSession().getMembersChannel(groupName);
            for (Channel channel : channels) {
                channel.writeAndFlush(new GroupJoinResponseMessage(true,username + "被添加到" + groupName + "组中。"));
            }
        }
    }
}
