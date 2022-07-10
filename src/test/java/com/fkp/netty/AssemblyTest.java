package com.fkp.netty;

import io.netty.channel.DefaultEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class AssemblyTest {

    /**
     * 以测试方法运行会出现主线程结束后其任务线程结束的情况
     */
    @Test
    public void EventLoopTest(){
        /**
         * 创建事件循环组
         *      1.NioEventLoopGroup:可以处理io事件，普通任务，定时任务，若不指定事件循环对象个数则默认为处理器核数的2倍
         *      2.DefaultEventLoopGroup:可以处理普通任务，定时任务
         */
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(2);
//        DefaultEventLoop defaultEventLoop = new DefaultEventLoop();
        //获取下一个事件循环对象，循环获取
        System.out.println(eventLoopGroup.next());
        System.out.println(eventLoopGroup.next());
        System.out.println(eventLoopGroup.next());
        System.out.println(eventLoopGroup.next());

        //执行普通任务，异步
        eventLoopGroup.next().submit(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.info("ok");
        });
        log.info("main");
    }
}
