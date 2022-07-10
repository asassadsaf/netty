package com.fkp.netty.assembly;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

@Slf4j
public class EventLoopServerTest {
    public static void main(String[] args) {
        //细分2：创建一个单独的EventLoopGroup用来处理比较耗时的业务逻辑，不占用worker的EventLoopGroup中的线程，也就不会使其管理的多个channel中的读写事件处于较长时间的等待状态，因为不处理io事件，创建DefaultEventLoopGroup
        DefaultEventLoopGroup defaultEventLoopGroup = new DefaultEventLoopGroup();
        new ServerBootstrap()
                //细分1：将EventLoopGroup分工更加明确，第一个参数的只负责ServerSocketChannel上的accept事件，第二个参数的只负责SocketChannel上的读写事件，即boss和worker
                .group(new NioEventLoopGroup(),new NioEventLoopGroup(2))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        //在一条流水线中添加多个handler,前一个handler最后要调用ChannelHandlerContext的fireChannelRead方法把这一步的数据传递到下一个handler中
                        nioSocketChannel.pipeline().addLast("handler1",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                String bufStr = buf.toString(StandardCharsets.UTF_8);
                                log.info("bufStr:{}",bufStr);
                                ctx.fireChannelRead(msg);
                            }
                            //指定处理这个handler的EventLoopGroup
                        }).addLast(defaultEventLoopGroup,"handler2",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                String bufStr = buf.toString(StandardCharsets.UTF_8);
                                log.info("bufStr:{}",bufStr);
                            }
                        });
                    }
                })
                .bind(new InetSocketAddress(9000));
    }
}
