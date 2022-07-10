package com.fkp.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class MultiThreadServer {

    public static void main(String[] args) {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
             Selector selector = Selector.open()) {
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(9000));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, null);
            //获取机器CPU的核心数
            int workerCount = Runtime.getRuntime().availableProcessors();
            List<Worker> workers = new ArrayList<>(workerCount);
            for (int i = 0; i < workerCount; i++) {
                workers.add(new Worker("worker-" + i));
            }
            AtomicInteger index = new AtomicInteger();
            while (true) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) {
                        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                        SocketChannel sc = ssc.accept();
                        sc.configureBlocking(false);
                        Worker currentWorker = workers.get(index.getAndIncrement() % workers.size());
                        log.info("currentWorker:{}", currentWorker.getName());
                        currentWorker.register(sc);
                    }
                }
            }
        } catch (IOException e) {
        }
    }
}
