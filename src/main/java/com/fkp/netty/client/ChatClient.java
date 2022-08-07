package com.fkp.netty.client;

import com.fkp.netty.config.Config;
import com.fkp.netty.message.*;
import com.fkp.netty.protocol.MessageCodecSharable;
import com.fkp.netty.protocol.MyFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ChatClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGINGHANDLER = new LoggingHandler(LogLevel.INFO);
        MessageCodecSharable MESSAGECODEC = new MessageCodecSharable();
        AtomicBoolean LOGIN = new AtomicBoolean(false);
        CountDownLatch WAIT_FOR_LOGIN = new CountDownLatch(1);
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            //添加CONNECT_TIMEOUT_MILLIS选项设置客户端的超时时间
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,1000);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                    nioSocketChannel.pipeline().addLast(new MyFrameDecoder());
//                    nioSocketChannel.pipeline().addLast(LOGGINGHANDLER);
                    nioSocketChannel.pipeline().addLast(MESSAGECODEC);
                    //用来判断是不是 读空闲时间过长 或 写空闲时间过长 或 读写空闲时间过长，如果超过规定时间则会触发相应的事件，IdleStateEvent
                    nioSocketChannel.pipeline().addLast(new IdleStateHandler(0,3,0));
                    //ChannelDuplexHandler 既可以作为入站处理器也可以作为出站处理器
                    nioSocketChannel.pipeline().addLast(new ChannelDuplexHandler(){
                        //用来触发特殊事件
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            IdleStateEvent event = (IdleStateEvent) evt;
                            //触发了读空闲事件
                            if(event.state() == IdleState.WRITER_IDLE){
                                //log.info("已经3s没有发送消息了，发送心跳数据...");
                                ctx.writeAndFlush(new PingMessage());
                            }
                        }
                    });
                    nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter(){

                        //接收响应消息
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            if (!(msg instanceof PongMessage)){
                                log.info("msg:{}",msg);
                            }
                            if(msg instanceof LoginResponseMessage){
                                if (((LoginResponseMessage) msg).isSuccess()) {
                                    LOGIN.set(true);
                                }
                                WAIT_FOR_LOGIN.countDown();
                            }
                        }

                        //channel建立好后出发active事件
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            //负责接收用户在控制台的输入，负责想服务器发送各种消息
                            new Thread(() -> {
                                Scanner scanner = new Scanner(System.in);
                                System.out.println("请输入用户名");
                                String username = scanner.nextLine();
                                System.out.println("请输入密码");
                                String password = scanner.nextLine();
                                //构造消息
                                LoginRequestMessage requestMessage = new LoginRequestMessage(username, password);
                                //发送消息
                                ctx.writeAndFlush(requestMessage);

                                System.out.println("等待后续操作...");
                                try {
                                    WAIT_FOR_LOGIN.await();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if(!LOGIN.get()){
                                    ctx.channel().close();
                                    return;
                                }
                                while (true){
                                    System.out.println("==================================");
                                    System.out.println("send [username] [content]");
                                    System.out.println("gsend [group name] [content]");
                                    System.out.println("gcreate [group name] [m1,m2,m3...]");
                                    System.out.println("gmembers [group name]");
                                    System.out.println("gjoin [group name]");
                                    System.out.println("gquit [group name]");
                                    System.out.println("quit");
                                    System.out.println("==================================");
                                    String selectMenu = scanner.nextLine();
                                    String[] s = selectMenu.split(" ");
                                    switch (s[0]){
                                        case "send":
                                            ctx.writeAndFlush(new ChatRequestMessage(username,s[1],s[2]));
                                            break;
                                        case "gsend":
                                            ctx.writeAndFlush(new GroupChatRequestMessage(username,s[1],s[2]));
                                            break;
                                        case "gcreate":
                                            HashSet<String> set = new HashSet<>(Arrays.asList(s[2].split(",")));
                                            set.add(username);
                                            ctx.writeAndFlush(new GroupCreateRequestMessage(s[1], set));
                                            break;
                                        case "gmembers":
                                            ctx.writeAndFlush(new GroupMembersRequestMessage(s[1]));
                                            break;
                                        case "gjoin":
                                            ctx.writeAndFlush(new GroupJoinRequestMessage(username,s[1]));
                                            break;
                                        case "gquit":
                                            ctx.writeAndFlush(new GroupQuitRequestMessage(username,s[1]));
                                            break;
                                        case "quit":
                                            ctx.channel().close();
                                            return;
                                    }
                                }
                            },"input").start();
                        }

                    });
                }
            });
            Channel channel = bootstrap.connect("localhost", Config.getServerPort()).sync().channel();
            channel.closeFuture().sync();
            log.info("client exit...");
            group.shutdownGracefully();
        } catch (Exception e) {
            e.printStackTrace();
            log.info("client error exit...");
            group.shutdownGracefully();
        }
    }
}
