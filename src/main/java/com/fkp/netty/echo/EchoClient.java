package com.fkp.netty.echo;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Slf4j
public class EchoClient {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Channel channel = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast(new LoggingHandler(LogLevel.TRACE));
                        //相当于StringEncoder
                        nioSocketChannel.pipeline().addLast(new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                ByteBuf buf = ctx.alloc().buffer().writeBytes(StandardCharsets.UTF_8.encode((String) msg));
                                ctx.writeAndFlush(buf);
                                buf.release();
                            }
                        });
                        nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                String s = ((ByteBuf) msg).toString(StandardCharsets.UTF_8);
                                log.info("echo:{}",s);
                            }
                        });
                    }
                })
                .connect(new InetSocketAddress("localhost", 18002))
                .sync()
                .channel();

        ChannelFuture channelFuture = channel.closeFuture();
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                //关闭EventloopGroup中所有线程，使程序结束
                group.shutdownGracefully();
            }
        });

        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            String next;
            while(!"q".equalsIgnoreCase(next = scanner.next())){
                channel.writeAndFlush(next);
            }
            channel.close();
        },"input").start();
    }
}
