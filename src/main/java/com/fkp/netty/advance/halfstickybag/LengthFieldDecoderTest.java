package com.fkp.netty.advance.halfstickybag;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.nio.charset.StandardCharsets;

public class LengthFieldDecoderTest {
    public static void main(String[] args) {
        EmbeddedChannel channel = new EmbeddedChannel(
            new LengthFieldBasedFrameDecoder(1024,8,4,4,16),
            new LoggingHandler(LogLevel.INFO)
        );
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        createByteBuf(buf,"hello, world");
        createByteBuf(buf,"hello, netty");
        createByteBuf(buf,"hello LengthFieldBasedFrameDecoder");
        channel.writeInbound(buf);
    }

    public static void createByteBuf(ByteBuf buf, String content){
        //数据格式为  8个字节msgId，4个字节内容长度，4个字节版本号，内容
        long msgId = 1L;
        int len = content.length();
        int version = 1;
        buf.writeLong(msgId);
        buf.writeInt(len);
        buf.writeInt(version);
        buf.writeBytes(content.getBytes(StandardCharsets.UTF_8));
    }
}
