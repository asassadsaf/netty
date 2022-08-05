package com.fkp.netty.protocol;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class HttpClientTest {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                    nioSocketChannel.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                    nioSocketChannel.pipeline().addLast(new HttpClientCodec());
                    nioSocketChannel.pipeline().addLast(new ChannelOutboundHandlerAdapter(){
                        @Override
                        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,"http://localhost:9000/index.html");
                            ctx.writeAndFlush(request);
                        }
                    });
                    //响应头，相应行
                    nioSocketChannel.pipeline().addLast(new SimpleChannelInboundHandler<HttpResponse>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpResponse httpResponse) throws Exception {
                            log.info("响应行:{}",httpResponse.protocolVersion().toString()+httpResponse.status());
                            log.info("响应头:{}",httpResponse.headers());
                            log.info("{}", httpResponse.getClass());
                        }
                    });
                    //响应体
                    nioSocketChannel.pipeline().addLast(new SimpleChannelInboundHandler<HttpContent>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpContent httpContent) throws Exception {
                            log.info("响应体:{}",httpContent.content().toString(StandardCharsets.UTF_8));
                            log.info("{}",httpContent.getClass());
                            channelHandlerContext.channel().close();
                        }
                    });
                }
            });
            ChannelFuture channelFuture = bootstrap.connect("localhost", 9000).sync();
            channelFuture.channel().writeAndFlush("");
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            group.shutdownGracefully();
        } finally {
            group.shutdownGracefully();
        }
    }


}
