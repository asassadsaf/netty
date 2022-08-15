package com.fkp.netty.server.handler;

import com.fkp.netty.message.RpcResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ChannelHandler.Sharable
//虽然有保存状态的信息，但是使用的ConcurrentHashMap可以保证线程安全，因此可以使用@Sharable注解
public class RpcResponseMessageHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {

    public static final Map<Integer, Promise<Object>> promiseMap = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage msg) throws Exception {
        log.info("{}",msg);
        //拿到promise之后需要移除，否则会越来越多
        Promise<Object> promise = promiseMap.remove(msg.getSequenceId());
        if(promise != null){
            Object returnValue = msg.getReturnValue();
            Exception exceptionValue = msg.getExceptionValue();
            if(exceptionValue != null){
                promise.setFailure(exceptionValue);
            }else {
                promise.setSuccess(returnValue);
            }
        }
    }
}
