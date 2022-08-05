package com.fkp.netty.protocol;

import com.fkp.netty.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.List;

@Slf4j
public class MessageCodec extends ByteToMessageCodec<Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        //1.4字节的魔数
        out.writeBytes(new byte[]{1,2,3,4});
        //2.1字节的版本号
        out.writeByte(1);
        //3.1字节的序列化方式：0 jdk，1 json
        out.writeByte(0);
        //4.1字节的指令类型
        out.writeByte(msg.getMessageType());
        //5.4字节的消息id
        out.writeInt(msg.getSequenceId());
        //6.无意义，对齐填充
        out.writeByte(0xff);
        //jdk方式序列化消息内容
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream);
        oos.writeObject(msg);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        //7.4字节内容长度
        out.writeInt(bytes.length);
        //8.len字节的内容
        out.writeBytes(bytes);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int magicNum = in.readInt();
        byte version = in.readByte();
        byte serializerType = in.readByte();
        byte messageType = in.readByte();
        int sequenceId = in.readInt();
        in.readByte();
        int len = in.readInt();
        byte[] data = new byte[len];
        in.readBytes(data);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        Message message = (Message) ois.readObject();
        out.add(message);
        log.info("magicNum:{},version:{},serializerType:{},messageType:{},sequenceId:{},len:{},data:{}",magicNum,version,serializerType,messageType,sequenceId,len,data);
        log.info("message:{}",message);
    }
}
