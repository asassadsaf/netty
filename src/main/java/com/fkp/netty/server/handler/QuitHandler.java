package com.fkp.netty.server.handler;

import com.fkp.netty.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@ChannelHandler.Sharable
@Slf4j
public class QuitHandler extends ChannelInboundHandlerAdapter {

    //当连接断开时触发inactive事件
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //从会话管理器中移除该用户的channel
        SessionFactory.getSession().unbind(ctx.channel());
        log.info("{}已断开",ctx.channel());
    }

    //当连接异常断开时触发exceptionCaught事件
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        SessionFactory.getSession().unbind(ctx.channel());
        log.info("{}异常断开，异常为:{}",ctx.channel(),cause.getMessage());

    }
}
