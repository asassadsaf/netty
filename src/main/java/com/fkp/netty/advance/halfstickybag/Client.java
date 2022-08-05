package com.fkp.netty.advance.halfstickybag;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Random;

public class Client {
    static void start() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                    nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        //在Channel建立完成后出发active事件，执行该方法
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            ByteBuf buffer = ctx.alloc().buffer();
                            Random random = new Random();
                            for (int i = 0; i < 10; i++) {
                                buffer.writeBytes(fill10bytes('a', random.nextInt(10) + 1));
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

    public static byte[] fill10bytes(char c, int len) {
        byte[] bytes = new byte[10];
        for (int i = 0; i < 10; i++) {
            if (i < len) {
                bytes[i] = (byte) c;
            } else {
                bytes[i] = '-';
            }
        }
        return bytes;
    }

    public static void main(String[] args) {
        start();
        System.out.println("finish...");

    }
}
