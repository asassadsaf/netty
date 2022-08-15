package com.fkp.netty.rpc;

import com.fkp.netty.message.RpcRequestMessage;
import com.fkp.netty.protocol.MessageCodecSharable;
import com.fkp.netty.protocol.MyFrameDecoder;
import com.fkp.netty.server.handler.RpcResponseMessageHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RpcClientManager {

    private static Channel channel = null;
    private static final Object LOCK = new Object();

    public static <T> T getProxyService(Class<T> clazz){
        ClassLoader classLoader = clazz.getClassLoader();
        Class<?>[] classes = {clazz};
        Object o = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                int sequenceId = 1;
                RpcRequestMessage request = new RpcRequestMessage(sequenceId, clazz.getName(), method.getName(), method.getReturnType(), method.getParameterTypes(), args);
                getChannel().writeAndFlush(request);
                DefaultPromise<Object> promise = new DefaultPromise<>(getChannel().eventLoop());
                RpcResponseMessageHandler.promiseMap.put(sequenceId, promise);
                //异步方式
//                promise.addListener(new GenericFutureListener<Future<? super Object>>() {
//                    @Override
//                    public void operationComplete(Future<? super Object> future) throws Exception {
//                        if(future.isSuccess()){
//                            getChannel().close();
//                        }else {
//                            throw new RuntimeException(future.cause());
//                        }
//                    }
//                });
//                return promise.get();
                promise.await();
                if (promise.isSuccess()) {
                    getChannel().close();
                    return promise.getNow();
                } else {
                    getChannel().close();
                    throw new RuntimeException(promise.cause());
                }
            }
        });
        return (T) o;
    }


    private static Channel getChannel(){
        if(channel != null){
            return channel;
        }
        synchronized (LOCK){
            if(channel != null){
                return channel;
            }
            initChannel();
            return channel;
        }
    }

    private static void initChannel(){
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler loggingHandler = new LoggingHandler(LogLevel.INFO);
        MessageCodecSharable messageCodecSharable = new MessageCodecSharable();
        RpcResponseMessageHandler rpcResponseMessageHandler = new RpcResponseMessageHandler();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(group);
            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(6*1024,12,4,0,0));
                    ch.pipeline().addLast(loggingHandler);
                    ch.pipeline().addLast(messageCodecSharable);
                    ch.pipeline().addLast(rpcResponseMessageHandler);
                }
            });
            channel = bootstrap.connect("localhost", 9000).sync().channel();
            channel.closeFuture().addListener(future -> {
                group.shutdownGracefully();
            });
        } catch (Exception e) {
            e.printStackTrace();
            group.shutdownGracefully();
        }
    }
}
