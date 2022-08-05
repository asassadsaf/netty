package com.fkp.netty.advance.halfstickybag;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Client2 {
    static void start() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                    nioSocketChannel.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                    nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        //在Channel建立完成后出发active事件，执行该方法
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            ByteBuf buffer = ctx.alloc().buffer();
                            Random random = new Random();
                            char c = '0';
                            for (int i = 0; i < 10; i++) {
                                int randomInt = random.nextInt(256) + 1;
                                buffer.writeBytes(makeStr(c, randomInt).getBytes(StandardCharsets.UTF_8));
                                c++;
                            }
                            ctx.writeAndFlush(buffer);
                            ctx.channel().close();
                        }
                    });
                }
            });
            ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress("localhost", 9000)).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static String makeStr(char c, int len) {
        System.out.println(len);
        StringBuilder builder = new StringBuilder(len+2);
        for (int i = 0; i < len; i++) {
            builder.append(c);
        }
        builder.append("\n");
        System.out.println(builder.length());
        return builder.toString();
    }

    public static void main(String[] args) {
        start();
        System.out.println("finish...");

    }
}
