package com.fkp.netty.server.handler;

import com.fkp.netty.message.PingMessage;
import com.fkp.netty.message.PongMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@ChannelHandler.Sharable
@Slf4j
public class PingMessageHandler extends SimpleChannelInboundHandler<PingMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, PingMessage pingMessage) throws Exception {
        log.info("收到来自{}的心跳数据包{}，响应心跳数据包。",channelHandlerContext.channel(),pingMessage);
        channelHandlerContext.writeAndFlush(new PongMessage());
    }
}
