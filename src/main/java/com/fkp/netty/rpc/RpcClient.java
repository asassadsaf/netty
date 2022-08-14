package com.fkp.netty.rpc;

import com.fkp.netty.message.RpcRequestMessage;
import com.fkp.netty.protocol.MessageCodecSharable;
import com.fkp.netty.protocol.MyFrameDecoder;
import com.fkp.netty.server.handler.RpcResponseMessageHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler loggingHandler = new LoggingHandler(LogLevel.INFO);
        MessageCodecSharable messageCodecSharable = new MessageCodecSharable();
        RpcResponseMessageHandler rpcResponseMessageHandler = new RpcResponseMessageHandler();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new MyFrameDecoder());
                    ch.pipeline().addLast(loggingHandler);
                    ch.pipeline().addLast(messageCodecSharable);
                    ch.pipeline().addLast(rpcResponseMessageHandler);
                }
            });
            Channel channel = bootstrap.connect("localhost", 9000).sync().channel();
            ChannelFuture future = channel.writeAndFlush(new RpcRequestMessage(1, "com.fkp.netty.server.service.HelloService", "sayHello", String.class, new Class<?>[]{String.class}, new String[]{"zhangsan"}));
            future.addListener(promise -> {
                if(!promise.isSuccess()){
                    Throwable cause = promise.cause();
                    log.error("error:{}",cause.toString());
                }
            });
            channel.closeFuture().sync();
            group.shutdownGracefully();
        }catch (Exception e){
            e.printStackTrace();
            group.shutdownGracefully();
        }
    }
}
