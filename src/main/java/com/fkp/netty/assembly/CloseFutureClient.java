package com.fkp.netty.assembly;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

@Slf4j
public class CloseFutureClient {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Channel channel = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                        nioSocketChannel.pipeline().addLast(new StringEncoder());

                    }
                })
                .connect(new InetSocketAddress("localhost", 9000))
                .sync()
                .channel();
        log.info("Client Channel:{}", channel);
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String next = scanner.nextLine();
                if ("q".equalsIgnoreCase(next)) {
                    channel.close();
                    //处理channel关闭后的操作不能写在这里，因为close方法也是异步的交给nio线程执行，close可能1s之后才能关闭，但当前线程马上会执行下边的代码
//                    log.info("关闭后的操作...");
                    break;
                }
                channel.writeAndFlush(next);

            }

        }, "input").start();

        //通过CloseFuture处理关闭后的操作

//        //1. 同步处理关闭
//        ChannelFuture closeFuture = channel.closeFuture();
//        //阻塞主线程，待关闭操作完成后恢复执行，主线程执行关闭后的操作
//        closeFuture.sync();
//        log.info("处理关闭后的操作...");

        //2. 异步处理关闭
        ChannelFuture closeFuture1 = channel.closeFuture();
        //使用回调对象，待关闭操作完成后由其他线程执行关闭后的操作
        closeFuture1.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                log.info("处理关闭后的操作...");
                //关闭EventloopGroup中所有线程，使程序结束
                group.shutdownGracefully();
            }
        });

    }
}
