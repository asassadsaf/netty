package com.fkp.netty.server.handler;

import com.fkp.netty.message.GroupCreateRequestMessage;
import com.fkp.netty.message.GroupCreateResponseMessage;
import com.fkp.netty.server.session.Group;
import com.fkp.netty.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;
import java.util.Set;

@ChannelHandler.Sharable
public class GroupCreateRequestMessageHandler extends SimpleChannelInboundHandler<GroupCreateRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupCreateRequestMessage msg) throws Exception {
        String groupName = msg.getGroupName();
        Set<String> members = msg.getMembers();
        //群管理器，创建群聊，创建成功返回null，组名存在导致的失败返回该组对象
        Group group = GroupSessionFactory.getGroupSession().createGroup(groupName, members);
        if(group != null){
            ctx.writeAndFlush(new GroupCreateResponseMessage(false,"Create group fail,groupName is exist."));
        }else {
            //通知组内在线成员
            List<Channel> channels = GroupSessionFactory.getGroupSession().getMembersChannel(groupName);
            for (Channel channel : channels) {
                channel.writeAndFlush(new GroupCreateResponseMessage(true,"您已被拉入群聊：" + groupName));
            }
            //通知创建者
            ctx.writeAndFlush(new GroupCreateResponseMessage(true,"Create group success."));
        }
    }
}
