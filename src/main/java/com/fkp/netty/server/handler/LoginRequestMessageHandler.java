package com.fkp.netty.server.handler;

import com.fkp.netty.message.LoginRequestMessage;
import com.fkp.netty.message.LoginResponseMessage;
import com.fkp.netty.server.service.UserServiceFactory;
import com.fkp.netty.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class LoginRequestMessageHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, LoginRequestMessage msg) throws Exception {
        String username = msg.getUsername();
        String password = msg.getPassword();
        boolean login = UserServiceFactory.getUserService().login(username, password);
        LoginResponseMessage response;
        if (login) {
            SessionFactory.getSession().bind(channelHandlerContext.channel(), username);
            response = new LoginResponseMessage(true, "login success");
        } else {
            response = new LoginResponseMessage(false, "username or password incorrect");
        }
        channelHandlerContext.writeAndFlush(response);
    }
}
