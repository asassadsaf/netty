package com.fkp.nio;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import utils.ByteBufferUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class SocketTest {
    /**
     * 模拟阻塞模式下服务端
     */
    @Test
    public void serverTest() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(9000));

            List<SocketChannel> channels = new ArrayList<>();
            while (true) {
                log.info("connecting...");
                SocketChannel socketChannel = serverSocketChannel.accept();
                log.info("connected...{}", socketChannel);
                channels.add(socketChannel);
                log.info("channels:{}", channels);
                for (SocketChannel channel : channels) {
                    ByteBuffer buffer = ByteBuffer.allocate(16);
                    log.info("brfore read...");
                    channel.read(buffer);
//                    ByteBufferUtil.debugAll(buffer);
                    log.info("read from channel:{}", channel);
                    buffer.flip();
                    System.out.println(Arrays.toString(buffer.array()));
                    buffer.clear();
                    log.info("after read...");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 模拟阻塞模式下的客户端
     */
    @Test
    public void clientTest() {
        try (SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.connect(new InetSocketAddress("localhost", 9000));
            log.info("connected to server...{}", socketChannel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 模拟非阻塞模式下服务端
     * 此模式下单线程可以处理多个连接并接收多个连接的数据，但没有链接时线程不会阻塞而是不断循环执行，浪费资源
     * 数据复制过程中线程实际还是阻塞的（AIO改进的地方）
     */
    @Test
    public void serverNoBlockTest() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(9000));
            //设置serverSocketChannel为非阻塞模式
            serverSocketChannel.configureBlocking(false);
            List<SocketChannel> channels = new ArrayList<>();
            while (true) {
                //开启非阻塞模式时accept方法不会阻塞线程，会继续往下执行，当没有连接时socketChannel为null
                SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel != null) {
                    //设置SocketChannel为非阻塞模式
                    socketChannel.configureBlocking(false);
                    channels.add(socketChannel);
                    log.info("connected...{}", socketChannel);
                }
                for (SocketChannel channel : channels) {
                    ByteBuffer buffer = ByteBuffer.allocate(16);
                    //socketChannel没有数据读时read方法不会阻塞线程，会继续往下执行，此时read方法返回0
                    int read = channel.read(buffer);
                    if (read > 0) {
                        buffer.flip();
                        ByteBufferUtil.debugAll(buffer);
                        log.info("read from channel:{}", channel);
                        log.info("after read...");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 模拟多路复用模式下服务端
     * 此模式下单线程可以处理多个连接并接收多个连接的数据，但没有链接时线程不会阻塞而是不断循环执行，浪费资源
     */
    @Test
    public void serverSelectorTest() {
        //创建一个selector
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
             Selector selector = Selector.open()) {
            serverSocketChannel.bind(new InetSocketAddress(9000));
            //设置serverSocketChannel为非阻塞模式
            serverSocketChannel.configureBlocking(false);
            //将serverSocketChannel注册到selector，返回一个SelectionKey，通过他可以知道哪个channel的事件发生
            SelectionKey sscKey = serverSocketChannel.register(selector, 0, null);
            //设置serverSocketChannel关注的事件，只关注accept事件
            sscKey.interestOps(SelectionKey.OP_ACCEPT);
            while (true) {
                //调用select方法，没有事件发生，线程阻塞，有时间线程恢复运行，当一个事件发生后要么接收要么取消，不能不做操作，select在事件未处理时不会阻塞线程
                selector.select();
                //获取注册到当前selector上的所有的channel的SelectionKey,通过SelectionKey可以获得是哪个channel产生了哪种事件，事件分为Accept,Connect,Read,Write
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                log.info("selectedKeys:{}", selector.selectedKeys());
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    //拿到SelectionKey后马上从SelectionKeys中删除
                    iterator.remove();
                    log.info("SelectionKey:{}", selectionKey);
                    //如果当前selectionKey事件为accept事件
                    if (selectionKey.isAcceptable()) {
                        //获取当前SelectionKey的channel
                        ServerSocketChannel ssc = (ServerSocketChannel) selectionKey.channel();
                        SocketChannel sc = ssc.accept();
                        sc.configureBlocking(false);
                        ByteBuffer buffer = ByteBuffer.allocate(16);
                        //attachment把buffer当作附件注册到当前channel对应的SelectionKey上
                        SelectionKey scKey = sc.register(selector, 0, buffer);
                        scKey.interestOps(SelectionKey.OP_READ);
                        log.info("SocketChannel:{}", sc);

                        //如果当前SelectionKey事件为Read事件
                    } else if (selectionKey.isReadable()) {
                        try {
                            SocketChannel sc = (SocketChannel) selectionKey.channel();
                            ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
                            int read = sc.read(buffer);
                            if (read > 0) {
                                ByteBufferTest.splitDate(buffer);
                            }
                            //buffer填满都没有遇到分隔符\n，此时需要扩容
                            if (buffer.position() == buffer.limit()) {
                                ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                                selectionKey.attach(newBuffer);
                                buffer.flip();
                                newBuffer.put(buffer);
                            }
                            //read=-1代表客户端正常关闭
                            if (read == -1) {
                                selectionKey.cancel();
                            }
                        } catch (Exception e) {
                            //客户端异常关闭时，将与客户端建立的连接即SocketChannel从Selector上反注册
                            e.printStackTrace();
                            selectionKey.cancel();
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void writeServer() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
             Selector selector = Selector.open()) {
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(9000));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
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
                        SelectionKey scKey = sc.register(selector, SelectionKey.OP_READ, null);
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < 30000000; i++) {
                            builder.append("a");
                        }
                        ByteBuffer buffer = StandardCharsets.UTF_8.encode(builder.toString());
                        int write = sc.write(buffer);
                        System.out.println(write);
                        //如果一次沒有写完
                        if (buffer.hasRemaining()) {
                            //给ScKey关注可写事件,且不能把原来的可读事件覆盖
                            scKey.interestOps(scKey.interestOps() + SelectionKey.OP_WRITE);
                            //将没有写完的buff挂在到scKey附件上
                            scKey.attach(buffer);
                        }
                    }else if(key.isWritable()){
                        SocketChannel sc = (SocketChannel) key.channel();
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        int write = sc.write(buffer);
                        System.out.println(write);
                        //buffer中没有剩余数据时要进行清楚工作
                        if(!buffer.hasRemaining()){
                            //清除挂载在scKey上的buffer清除
                            key.attach(null);
                            //删除scKey上的可写事件
                            key.interestOps(key.interestOps() - SelectionKey.OP_WRITE);

                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void writeCLient() {
        try (SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.connect(new InetSocketAddress("localhost", 9000));
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
            int count = 0;
            while (true) {
                count += socketChannel.read(buffer);
                System.out.println(count);
                buffer.clear();
            }
        } catch (IOException e) {
        }
    }

}
