package com.fkp.netty.protocol;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

@Slf4j
public class HttpServerTest {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                    nioSocketChannel.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                    nioSocketChannel.pipeline().addLast(new HttpServerCodec());
                    //可以通过SimpleChannelInboundHandler的泛型指定感兴趣的类型
                    nioSocketChannel.pipeline().addLast(new SimpleChannelInboundHandler<HttpRequest>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpRequest httpRequest) throws Exception {
                            log.info("请求行:{}",httpRequest.uri());
                            log.info("请求头:{}",httpRequest.headers());

                            //返回响应
                            DefaultFullHttpResponse response = new DefaultFullHttpResponse(httpRequest.protocolVersion(), HttpResponseStatus.OK);
                            byte[] content = "<h1>hello, world!</h1>".getBytes(StandardCharsets.UTF_8);
                            //设置响应头的内容长度，否则浏览器会一直在加载状态
                            response.headers().setInt(CONTENT_LENGTH,content.length);
                            response.content().writeBytes(content);
                            channelHandlerContext.writeAndFlush(response);
                        }
                    });
//                    nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
//                        @Override
//                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                            log.info("{}",msg.getClass());
//                            //通过打印msg类型可知会接收两个对象一个是DefaultHttpRequest,一个为LastHttpContent，可通过其接口来判断
//                            if(msg instanceof HttpRequest){
//                                //请求行和请求头
//                            }else if(msg instanceof HttpContent){
//                                //请求体
//                            }
//                        }
//                    });
                }
            });
            ChannelFuture channelFuture = bootstrap.bind(9000).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            group.shutdownGracefully();
        } finally {
            group.shutdownGracefully();
        }


    }
}
