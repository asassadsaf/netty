package com.fkp.netty.echo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

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
                        nioSocketChannel.pipeline().addLast("myHaProxyDecoder", new MyHaProxyDecoder());
//                        nioSocketChannel.pipeline() .addLast(new HAProxyMessageDecoder());
//                        nioSocketChannel.pipeline() .addLast(new HAProxyMessageDecoder());
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
    static class MyHaProxyDecoder extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (ctx.channel().attr(AttributeKey.newInstance("haProxyHeader")).get() == null) {
                ByteBuf byteBuf = (ByteBuf) msg;
                String headerContent = byteBuf.toString(StandardCharsets.UTF_8);
                int i = StringUtils.countMatches(headerContent, "PROXY");
                for (int j = 0; j < i; j++) {
                    ctx.channel().pipeline().addAfter("myHaProxyDecoder","haProxyDecoder-" + j, new HAProxyMessageDecoder());
                }
                ctx.fireChannelRead(msg);
                ctx.channel().attr(AttributeKey.valueOf("haProxyHeader")).set("true");
            }else {
                ctx.channel().pipeline().remove(this);
            }

        }
    }
}
