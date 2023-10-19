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
                        nioSocketChannel.pipeline().addLast("loggingHandler", new LoggingHandler(LogLevel.INFO));
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
//                                    response.release();
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
    static class MyHaProxyDecoder extends HAProxyMessageDecoder {
        long initTime = System.currentTimeMillis();

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//            ctx.channel().attr(AttributeKey.valueOf("haProxyHeader")).get() == null ||
            if ((System.currentTimeMillis() - (1000 * 10)) < initTime) {
//                ByteBuf byteBuf = (ByteBuf) msg;
//                String headerContent = byteBuf.toString(StandardCharsets.UTF_8);
//                int i = StringUtils.countMatches(headerContent, "PROXY");
//                for (int j = 0; j < i; j++) {
//                    ctx.channel().pipeline().addAfter("myHaProxyDecoder","haProxyDecoder-" + j, new HAProxyMessageDecoder());
//                }
                ctx.channel().pipeline().addAfter("myHaProxyDecoder", "haProxyMessageDecoder1", new HAProxyMessageDecoder());
                ctx.channel().pipeline().addAfter("myHaProxyDecoder", "haProxyMessageDecoder1", new HAProxyMessageDecoder());
                super.channelRead(ctx, msg);
                ctx.channel().pipeline().addAfter("loggingHandler", "myHaProxyDecoder", this);
//                ctx.channel().attr(AttributeKey.valueOf("haProxyHeader")).set("true");
            }else {
//                if (ctx.channel().pipeline().get(HAProxyMessageDecoder.class) != null) {
//                    ctx.channel().pipeline().remove(HAProxyMessageDecoder.class);
//                }
                ByteBuf byteBuf = super.internalBuffer();
                byteBuf.writeBytes((ByteBuf) msg);
                ctx.fireChannelRead(byteBuf);
                ctx.channel().pipeline().remove(this);
            }
        }


    }
}
