package com.fkp.netty.protocol;

import com.fkp.netty.message.LoginRequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class MessageCodecTest {
    public static void main(String[] args) throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel(
                new LoggingHandler(LogLevel.INFO),
                new LengthFieldBasedFrameDecoder(1024,12,4,0,0),
                new MessageCodecSharable()
        );
        LoginRequestMessage requestMessage = new LoginRequestMessage("zhangsan","123");
        //channel.writeOutbound(requestMessage);

        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null,requestMessage,buffer);
        ByteBuf slice = buffer.slice(0, buffer.readableBytes() / 2);
        slice.retain();
        ByteBuf slice1 = buffer.slice(buffer.readableBytes() / 2, buffer.readableBytes() - (buffer.readableBytes() / 2));
        channel.writeInbound(slice);
        channel.writeInbound(slice1);


    }
}
