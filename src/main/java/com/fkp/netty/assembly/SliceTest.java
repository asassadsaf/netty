package com.fkp.netty.assembly;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.extern.slf4j.Slf4j;

import static com.fkp.netty.assembly.ByteBufTest.log;

@Slf4j
public class SliceTest {
    public static void main(String[] args) {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(10);
        buffer.writeBytes(new byte[]{'a','b','c','d','e','f','g','h','i','j',});
        log(buffer);
        //slice分片不会产生数据的复制,从下标i开始长度为i1的切片
        ByteBuf buf = buffer.slice(0, 5);
        ByteBuf buf2 = buffer.slice(5, 5);
        log(buf);
        log(buf2);

        //不改变写指针的情况下通过下标索引写入数据,因为分片操作数据还是在原来的内存上，改变切片内容会影响原来的数据
        //buf.setByte(0,'b');
//        log(buf);
//        log(buffer);

        //释放原有的buf内存后切片后的数据也不能使用，如果释放原有buf内存后想继续使用切片后的buf，可以使用retain，所以一般在切片后都会使用retain让引用计数+1,防止原来的buf内存释放后导致分片的buf不能使用
        buf.retain();
        buf2.retain();
        log.info("释放原来buffer内存");
        //buffer.release();
        log.info("分片数据buf：");
        log(buf);
        //分片数据使用完后释放内存
        buf.release();
        buf2.release();



        //切片后的buf不允许写入新数据
        //buf.writeByte('x');

        //duplicate方法相当于对整个buf进行分片,使用与slice相同
        ByteBuf duplicate = buffer.duplicate();
        duplicate.retain();
        log(duplicate);
        duplicate.release();

        //copy方法是深拷贝，将原来buf内存数据复制一份放入新的内存空间中，因此copy出来的buf与原来的buf相互没有影响
        ByteBuf copy = buffer.copy();
        copy.writeByte('x');
        log(copy);
        log(buffer);
        copy.release();
        buffer.release();


    }
}
