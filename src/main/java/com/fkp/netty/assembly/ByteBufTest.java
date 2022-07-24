package com.fkp.netty.assembly;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.extern.slf4j.Slf4j;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

@Slf4j
public class ByteBufTest {
    //ByteBuf的创建
    public static void main(String[] args) {
//        通过指定虚拟机参数或配置环境变量可以更改bytebuf为非池化，添加参数-Dio.netty.allocator.type=unpooled

        //创建堆内存的bytebuf
        ByteBuf buf1 = ByteBufAllocator.DEFAULT.heapBuffer();
        log.info("buf1Class:{}",buf1.getClass());

        //不指定时初始容量为256byte，当超过容量时会自动扩容为原来的两倍
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        log.info("bufClass:{}",buf.getClass());
        log(buf);
        StringBuilder builder = new StringBuilder();
        for(int i=0;i<32;i++){
            builder.append("a");
        }
        buf.writeBytes(builder.toString().getBytes());
        log(buf);

        //--------------ByteBuf组成-----------------------
//        四个指针四个区域
//             读指针            写指针                容量                             最大容量
//     |         |                 |                    |                                   |
//      废弃区域      可读区域            可写区域                   可扩容区域


    }

    //调试方法
    public static void log(ByteBuf buf){
        int length = buf.readableBytes();
        int rows = length / 16 + (length % 15 == 0 ? 0 : 1) + 4;
        StringBuilder builder = new StringBuilder(rows * 80 * 2)
                .append("read index:").append(buf.readerIndex())
                .append(" write index:").append(buf.writerIndex())
                .append(" capacity:").append(buf.capacity())
                .append(NEWLINE);
        appendPrettyHexDump(builder,buf);
        System.out.println(builder.toString());
    }
}
