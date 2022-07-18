package com.fkp.netty.assembly;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class PipelineTest {
    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        //通过socketChannel拿到pipeline
                        ChannelPipeline pipeline = nioSocketChannel.pipeline();
                        //netty默认有两个handler:head -> tail，pipeline为双向链表
                        //添加后的handler链为head -> h1 -> h2 -> h3 -> h4 -> tail
                        pipeline.addLast("h1",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.info("1");
                                ByteBuf buf = (ByteBuf) msg;
                                String name = buf.toString(StandardCharsets.UTF_8);
                                //将加工结果传到下一个handler
                                super.channelRead(ctx,name);
                            }
                        });
                        pipeline.addLast("h2",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                //这个方法接收的msg为上一步处理后传递过来的字符串
                                log.info("2");
                                Student student = new Student(msg.toString());
                                super.channelRead(ctx,student);
                            }
                        });
                        pipeline.addLast("h3",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.info("3,结果{},class:{}",msg,msg.getClass());
                                super.channelRead(ctx,msg);
                            }
                        });
                        pipeline.addLast("h4",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.info("4");
                                //只是传递到下一个入站处理器，下一个为出栈处理器则没有意义
                                super.channelRead(ctx,msg);
                                nioSocketChannel.writeAndFlush(ctx.alloc().buffer().writeBytes("server...".getBytes()));
                            }
                        });
                        pipeline.addLast("h5",new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.info("5,{}",((ByteBuf) msg).toString(StandardCharsets.UTF_8));
                                super.write(ctx, msg, promise);
                            }
                        });
                        pipeline.addLast("h6",new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.info("6");
                                //ChannelHandlerContext调用的写方法从当前handler往前找出栈处理器，NioSocketChannel的写方法是从尾处理器tail往前找出栈处理器
                                ctx.writeAndFlush(ctx.alloc().buffer().writeBytes("hello...".getBytes()));
                                super.write(ctx, msg, promise);
                            }
                        });
                        pipeline.addLast("h7",new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.info("7");
                                super.write(ctx, msg, promise);
                            }
                        });
                    }
                }).bind(9000);
    }
    @Data
    @AllArgsConstructor
    static class Student{
        private String name;
    }
}
