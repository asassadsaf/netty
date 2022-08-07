package com.fkp.netty.server;

import com.fkp.netty.config.Config;
import com.fkp.netty.message.PongMessage;
import com.fkp.netty.protocol.MessageCodecSharable;
import com.fkp.netty.protocol.MyFrameDecoder;
import com.fkp.netty.server.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChatServer {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        LoggingHandler loggingHandler = new LoggingHandler(LogLevel.INFO);
        MessageCodecSharable messageCodec = new MessageCodecSharable();
        LoginRequestMessageHandler loginRequestMessageHandler = new LoginRequestMessageHandler();
        ChatRequestMessageHandler chatRequestMessageHandler = new ChatRequestMessageHandler();
        GroupCreateRequestMessageHandler groupCreateRequestMessageHandler = new GroupCreateRequestMessageHandler();
        GroupChatRequestMessageHandler groupChatRequestMessageHandler = new GroupChatRequestMessageHandler();
        QuitHandler quitHandler = new QuitHandler();
        PingMessageHandler pingMessageHandler = new PingMessageHandler();
        GroupJoinRequestMessageHandler groupJoinRequestMessageHandler = new GroupJoinRequestMessageHandler();
        GroupQuitRequestMessagehandler groupQuitRequestMessagehandler = new GroupQuitRequestMessagehandler();
        GroupMembersRequestMessageHandler groupMembersRequestMessageHandler = new GroupMembersRequestMessageHandler();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss,worker);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel nioServerSocketChannel) throws Exception {
                    nioServerSocketChannel.pipeline().addLast(new MyFrameDecoder());
                    nioServerSocketChannel.pipeline().addLast(loggingHandler);
                    //用来判断是不是 读空闲时间过长 或 写空闲时间过长 或 读写空闲时间过长，如果超过规定时间则会触发相应的事件，IdleStateEvent
                    nioServerSocketChannel.pipeline().addLast(messageCodec);
                    nioServerSocketChannel.pipeline().addLast(new IdleStateHandler(5,0,0));
                    //ChannelDuplexHandler 既可以作为入站处理器也可以作为出站处理器
                    nioServerSocketChannel.pipeline().addLast(new ChannelDuplexHandler(){
                        //用来触发特殊事件
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            IdleStateEvent event = (IdleStateEvent) evt;
                            //触发了读空闲事件
                            if(event.state() == IdleState.READER_IDLE){
                                log.info("已经5s没有读到数据了...");
                                ctx.channel().close();
                            }
                        }
                    });
                    nioServerSocketChannel.pipeline().addLast(loginRequestMessageHandler);
                    nioServerSocketChannel.pipeline().addLast(chatRequestMessageHandler);
                    nioServerSocketChannel.pipeline().addLast(groupCreateRequestMessageHandler);
                    nioServerSocketChannel.pipeline().addLast(groupChatRequestMessageHandler);
                    nioServerSocketChannel.pipeline().addLast(quitHandler);
                    nioServerSocketChannel.pipeline().addLast(pingMessageHandler);
                    nioServerSocketChannel.pipeline().addLast(groupJoinRequestMessageHandler);
                    nioServerSocketChannel.pipeline().addLast(groupQuitRequestMessagehandler);
                    nioServerSocketChannel.pipeline().addLast(groupMembersRequestMessageHandler);

                }
            });
            Channel channel = serverBootstrap.bind(Config.getServerPort()).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error("server error exit....");
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        } finally {
            log.info("server exit...");
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
