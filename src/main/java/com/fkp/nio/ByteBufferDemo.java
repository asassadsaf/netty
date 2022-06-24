package com.fkp.nio;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 使用FileChannel读取文件
 */
@Slf4j
public class ByteBufferDemo {

    public static void main(String[] args) {

        //可以通过输入输出流和RandomAccessFile获取Channel
        try (FileChannel channel = new FileInputStream("file/abc.txt").getChannel()) {
            //准备缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(10);
            int len;
            //从channel读取数据写入buffer，当没有数据时放回-1
            while ((len = channel.read(buffer)) != -1){
                log.info("读取到的字节数:{}",len);
                //切换至读模式
                buffer.flip();
                //buffer中是否还有剩余数据
                while (buffer.hasRemaining()){
                    //一个字节一个字节的从buffer中读取数据
                    byte b = buffer.get();
                    log.info("实际字节:{}",(char) b);
                }
                //切换为写模式
                buffer.clear();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
