package com.fkp.netty.assembly;

import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class EventLoopTest {
    public static void main(String[] args) {
        /**
         * 1.创建事件循环组
         *      1.NioEventLoopGroup:可以处理io事件，普通任务，定时任务，若不指定事件循环对象个数则默认为处理器核数的2倍
         *      2.DefaultEventLoopGroup:可以处理普通任务，定时任务
         */
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(2);
//        DefaultEventLoop defaultEventLoop = new DefaultEventLoop();

        //2.获取下一个事件循环对象，循环获取
        System.out.println(eventLoopGroup.next());
        System.out.println(eventLoopGroup.next());
        System.out.println(eventLoopGroup.next());
        System.out.println(eventLoopGroup.next());

        //3.执行普通任务，异步
//        eventLoopGroup.next().submit(() -> {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            log.info("ok");
//        });
//        log.info("main");

        //4.执行定时任务，异步,参数分别为：任务，初始延迟时间，间隔时间，单位
        eventLoopGroup.scheduleAtFixedRate(() -> {
            log.info("ok");
        },0,1, TimeUnit.SECONDS);


    }
}
