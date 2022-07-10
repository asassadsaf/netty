package com.fkp.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public class Worker implements Runnable{
    private String name;
    private Selector selector;
    private Thread thread;
    private boolean start = false;
    ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();

    public Worker(String name) {
        this.name = name;
    }

    public void register (SocketChannel sc) throws IOException {
        if(!start){
            this.selector = Selector.open();
            this.thread = new Thread(this,name);
            thread.start();
            this.start = true;
        }
        queue.add(() -> {
            try {
                log.info("register before");
                sc.register(selector, SelectionKey.OP_READ,null);
                log.info("register after");
            } catch (ClosedChannelException e) {
                throw new RuntimeException(e);
            }
        });
        selector.wakeup();
    }

    @Override
    public void run() {
        while (true){
            try {
                selector.select();
                Runnable poll = queue.poll();
                if(poll != null){
                    poll.run();
                }
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()){
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isReadable()) {
                        SocketChannel sc = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(16);
                        int read = sc.read(buffer);
                        log.info("read...{}",read);
                        if(read > 0){
                            buffer.flip();
                            CharBuffer decode = StandardCharsets.UTF_8.decode(buffer);
                            System.out.println(decode);
                        }
                        if(read == -1){
                            key.cancel();
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String getName() {
        return name;
    }
}
