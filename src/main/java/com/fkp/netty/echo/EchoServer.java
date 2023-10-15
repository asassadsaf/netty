package com.fkp.netty.echo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class EchoServer {
    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast(new LoggingHandler(LogLevel.TRACE));
                        nioSocketChannel.pipeline() .addLast(new HAProxyMessageDecoder());
                        nioSocketChannel.pipeline() .addLast(new HAProxyMessageDecoder());
                        nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter(){

                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                log.info("channel active..");
                            }

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                if(msg instanceof HAProxyMessage){
                                    HAProxyMessage haProxyMessage = (HAProxyMessage) msg;
                                    String destinationAddress = haProxyMessage.destinationAddress();
                                    int destinationPort = haProxyMessage.destinationPort();
                                    String sourceAddress = haProxyMessage.sourceAddress();
                                    int sourcePort = haProxyMessage.sourcePort();
                                    log.info("destinationAddress:{}, destinationPort:{}, sourceAddress:{}, sourcePort:{}", destinationAddress, destinationPort, sourceAddress, sourcePort);
                                }else {
                                    ByteBuf byteBuf = (ByteBuf) msg;
                                    log.info("收到消息:{}",byteBuf.toString(StandardCharsets.UTF_8));
                                    ByteBuf response = ctx.alloc().buffer();
                                    response.writeBytes(byteBuf);
                                    ctx.writeAndFlush(response);
                                    response.release();
                                }
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                cause.printStackTrace();
                            }
                        });
                    }
                }).bind(9000);
    }
}
