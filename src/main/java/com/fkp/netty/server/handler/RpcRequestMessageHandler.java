package com.fkp.netty.server.handler;

import com.fkp.netty.message.RpcRequestMessage;
import com.fkp.netty.message.RpcResponseMessage;
import com.fkp.netty.server.service.ServicesFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;

public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage msg) throws Exception {
        RpcResponseMessage response = new RpcResponseMessage();
        response.setSequenceId(msg.getSequenceId());
        try {
            String interfaceName = msg.getInterfaceName();
            String methodName = msg.getMethodName();
            Class<?>[] parameterTypes = msg.getParameterTypes();
            Object[] parameterValue = msg.getParameterValue();
            //通过接口class类获取真正的实现类
            Object service = ServicesFactory.getService(Class.forName(interfaceName));
            //获取要调用的方法
            Method method = service.getClass().getMethod(methodName, parameterTypes);
            //方法调用
            Object invoke = method.invoke(service, parameterValue);
            //调用成功
            response.setReturnValue(invoke);
        }catch (Exception e){
            e.printStackTrace();
            //调用失败
            response.setExceptionValue(e);
        }
        //返回结果
        ctx.writeAndFlush(response);
    }
}
