package com.fkp.netty.assembly;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;

import static com.fkp.netty.assembly.ByteBufTest.log;

public class CompositeByteBufTest {
    public static void main(String[] args) {
        //CompositeByteBuf可以将两个ByteBuf拼接为一个buf且不发生数据复制
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        ByteBuf buf2 = ByteBufAllocator.DEFAULT.buffer();
        buf.writeBytes(new byte[]{'a','b','c','d','e'});
        buf2.writeBytes(new byte[]{'f','g','h','i','j'});

        CompositeByteBuf buffer = ByteBufAllocator.DEFAULT.compositeBuffer();
        //如果不加以一个参数，则写指针不会发生改变
        buffer.addComponents(true,buf,buf2);
        //在添加的各部分的buf调用retain，防止原来的buf释放后CompositeByteBuf无法使用
        buf.retain();
        buf2.retain();
        log(buffer);
        log(buf);
        log(buf2);
        buf.release();
        buf2.release();
        //与分片一样也是需要retain防止原来的buf释放了内存导致compositeByteBuf无法使用,但实际需要对各部分的buf进行retain，即使对compositeByteBuf进行了retain，但各部分已经释放了，compositeByteBuf也无法使用
        log(buffer);
        //通过CompositeByteBuf调用release可以释放组成其各部分的内存
        buffer.release();
    }
}
