package com.fkp.netty.param;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class BacklogServerTest {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss,worker);
            //通过配置服务端参数，即配置NioServerSocketChannel参数SO_BACKLOG来限制全连接队列中的最大连接数，超过设置的连接数，客户端的连接会被拒绝，需要在netty源码中打断点，阻止accept事件的操作，NioEventLoop.processSelectedKey方法
            serverBootstrap.option(ChannelOption.SO_BACKLOG,2);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                }
            });
            Channel channel = serverBootstrap.bind(9000).sync().channel();
            channel.closeFuture().sync();
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }catch (Exception e){
            e.printStackTrace();
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }

    }
}
