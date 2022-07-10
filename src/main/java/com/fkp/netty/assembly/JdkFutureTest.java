package com.fkp.netty.assembly;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class JdkFutureTest {
    static String xx;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        String bb = "bb";
        Integer[] aa = new Integer[1];
        ExecutorService service = Executors.newFixedThreadPool(2);
//        Future<Integer> future = service.submit(new Callable<Integer>() {
//            @Override
//            public Integer call() throws Exception {
//                log.info("执行计算...");
//                Thread.sleep(1000);
//                aa[0] = 50;
//                log.info("拿到主线程中定义的bb:{}",bb);
//                xx = "xx";
//                return 50;
//            }
//        });
        class aaa implements Callable<Integer>{
            @Override
            public Integer call() throws Exception {
                log.info("执行计算...");
                Thread.sleep(1000);
                aa[0] = 50;
                log.info("拿到主线程中定义的bb:{}",bb);
                xx = "xx";
                return 50;
            }
        }
        Future<Integer> future = service.submit(new aaa());
        log.info("等待结果...");
        //阻塞当前线程，直到拿到结果或失败抛出异常
        Integer result = future.get();
        log.info("拿到结果:{}",result);
        log.info("拿到结果在线程池中线程赋值的aa:{}",aa[0]);
        log.info("拿到结果在线程池中线程赋值的类中定义的静态变量xx:{}",xx);


    }
}
