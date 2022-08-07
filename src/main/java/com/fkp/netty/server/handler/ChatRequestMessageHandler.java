package com.fkp.netty.server.handler;

import com.fkp.netty.message.ChatRequestMessage;
import com.fkp.netty.message.ChatResponseMessage;
import com.fkp.netty.server.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class ChatRequestMessageHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ChatRequestMessage msg) throws Exception {
        String to = msg.getTo();
        Channel channel = SessionFactory.getSession().getChannel(to);
        if(channel != null){
            //对方在线
            channel.writeAndFlush(new ChatResponseMessage(msg.getFrom(),msg.getContent()));
        }else {
            channelHandlerContext.writeAndFlush(new ChatResponseMessage(false,"Target user not online or not exist."));
        }
    }
}
