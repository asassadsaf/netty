package com.fkp.netty.assembly;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

@Slf4j
public class NettyPromiseTest {
    public static void main(String[] args) {
        //1. 准备一个EventLoop对象，相当于一个线程
        EventLoop eventLoop = new NioEventLoopGroup().next();
        //2. 创建Promise对象，结果容器
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);
        //3. 任意一个线程执行计算，结果可以填充到promise对象中
        new Thread(() -> {
            log.info("执行计算");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                //填充结果
                int i = 1/0;
                promise.setSuccess(80);
            } catch (Exception e) {
                e.printStackTrace();
                promise.setFailure(e);
            }


        }).start();
        //4. 主线程拿结果，可以使用同步阻塞方法get,也可以使用异步非阻塞方法addListener
        log.info("等待结果");
        Integer res = null;
        try {
            res = promise.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        log.info("拿到结果:{}",res);

//        promise.addListener(new GenericFutureListener<Future<? super Integer>>() {
//            @Override
//            public void operationComplete(Future<? super Integer> future) throws Exception {
//                try {
//                    Object res = future.get();
//                    log.info("拿到结果:{}",(Integer) res);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//
//            }
//        });

    }
}
