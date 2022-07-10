package com.fkp.netty.assembly;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class EventLoopClientTest {
    public static void main(String[] args) throws InterruptedException {
        //带有Future,Promise的类型都是和异步方法配套使用，用来处理结果
        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast(new StringEncoder());
                    }
                })
                //connect方法为异步非阻塞方法，main发起调用，真正执行连接操作的是Nio线程
                .connect(new InetSocketAddress("localhost", 9000));
        //若没有sync方法则主线程继续向下执行，此时连接还没有建立好，因此导致channel拿不到真正可以发送接收数据的的channel
        //1. sync方法阻塞当前线程，直到nio线程建立连接完毕
//        ChannelFuture channelFuture1 = channelFuture.sync();
//        Channel channel = channelFuture1.channel();
        //因为netty是多线程的，真正处理写操作的不一定是主线程，因此需要更改idea断点模式，默认是主线程停在端点处时其他线程也停止运行，需要改为Thread,即主线程停在断在处不影响其他线程运行
//        log.info("Client Channel:{}", channel);

        //2. 使用addListener(回调对象)方法异步处理结果
        channelFuture.addListener(new ChannelFutureListener() {
            //在nio线程建立好连接之后调用operationComplete方法
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                Channel channel = channelFuture.channel();
                log.info("Client Channel:{}", channel);
                channel.writeAndFlush("hello netty!");
            }
        });
    }
}
