package com.fkp.nio;

import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.base64.Base64;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.util.Base64Utils;
import utils.ByteBufferUtil;

import java.io.*;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ByteBufferTest {
    /**
     * 测试ByteBuffer的flip,clear,compact方法
     */
    @Test
    public void test() {
        //使用try-with-resources虽然写起来比较麻烦但是可以自动释放io流   调用FileInputStream OutputStream RandomAccessFile的close会间接调用channel的close，不需要单独对channel调用close
        ByteBuffer buffer = ByteBuffer.allocate(14);
        byte[] bytes = new byte[10];
        try (FileInputStream fileInputStream = new FileInputStream("file/abc.txt")) {
            try (FileChannel channel = fileInputStream.getChannel()) {
                int read = channel.read(buffer);
                log.info("写入buffer的字节数：{}", read);
                //切换buffer至读模式
                buffer.flip();
                //从buffer读数据到byte数组，此时buffer中还有剩余
                buffer.get(bytes, 0, 10);
                log.info("读到byte数组中的数据：{}", bytes);
                byte[] array = buffer.array();
                log.info("buffer中剩余数据：{}", array);
                //调用clear方法会将剩余的数据清除position指向buffer头，切换至写模式
                buffer.clear();
                log.info("调用clear方法后buffer中数据：{}", buffer.array());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (FileInputStream fileInputStream = new FileInputStream("file/abc.txt")) {
            try (FileChannel channel = fileInputStream.getChannel()) {
                int read1 = channel.read(buffer);
                log.info("再次读入buffer中字节数：{}", read1);
                //切换buffer为读模式
                buffer.flip();
                //从buffer读数据到byte数据
                buffer.get(bytes);
                log.info("此时buffer中剩余数据：{}", buffer.array());
                //调用compact方法将把剩余的数据挪动到buffer开头，position指向剩余数据的最后，切换到写模式
                buffer.compact();
                log.info("调用compact方法后buffer中的数据：{}", buffer.array());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //再次向buffer中写入数据
        try (FileInputStream fileInputStream = new FileInputStream("file/abc.txt")) {
            try (FileChannel channel = fileInputStream.getChannel()) {
                int read2 = channel.read(buffer);
                log.info("调用compact方法后重新读入数据后buffer字节数：{}", read2);
                log.info("此时buffer中数据：{}", buffer.array());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ByteBuffer的常见方法
     */
    @Test
    public void test2() {
        //申请buffer的空间：allocate申请的时JVM的堆内存，读写性能较低，受GC影响，buffer在内存中可能会频繁挪动
        ByteBuffer buffer = ByteBuffer.allocate(10);
        //申请buffer的空间：allocateDirect申请的是操作系统的直接内存，读写性能较高，不受GC影响，但申请空间时较慢，因为要调用操作系统方法
        ByteBuffer buffer2 = ByteBuffer.allocateDirect(10);

        //向buffer中写数据：通过channel的read方法向buffer中写数据
        try (FileInputStream fileInputStream = new FileInputStream("file/abc.txt")) {
            try (FileChannel channel = fileInputStream.getChannel()) {
                channel.read(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //通过buffer的put方法
        buffer2.put(new byte[]{0x61, 0x62, 0x63, 0x64, 0x65});

        //从buffer读出数据：通过channel的write方法从buffer中读出数据
        try (RandomAccessFile randomAccessFile = new RandomAccessFile("file/aaa.txt", "rw")) {
            try (FileChannel channel = randomAccessFile.getChannel()) {
                buffer.flip();
                channel.write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //通过buffer的get方法
        byte[] bytes = new byte[5];
        buffer2.flip();
        buffer2.get(bytes);
        log.info("bytes:{}", bytes);

        //mark方法可以记录当前position的位置，当调用reset方法时position会回到mark记录的地方
        buffer.clear();
        buffer.put(new byte[]{0x61, 0x62, 0x63, 0x64, 0x65});
        buffer.flip();
        log.info("此时buffer中的数据为：{}", buffer.array());
        log.info("此时buffer的position位置为：{}", buffer.position());
        buffer.get();
        buffer.get();
        log.info("buffer读出两个字节后position的位置：{}", buffer.position());
        buffer.mark();
        buffer.get();
        buffer.get();
        buffer.reset();
        log.info("buffer标记后读取两个字节调用reset后position的位置：{}", buffer.position());

        //调用rewind方法将position指向开头
        buffer.rewind();
        log.info("调用rewind方法后buffer的position位置：{}", buffer.position());

        //调用get方法指定下标索引获取元素时不影响position位置
        byte b = buffer.get(0);
        byte b1 = buffer.get(1);
        log.info("通过get(index i)方法从buffer获取两个元素0，1：{}，{}", (char) b, (char) b1);
        log.info("此时buffer的position位置：{}", buffer.position());
    }

    /**
     * ByteBuffer和字符串互转
     */
    @Test
    public void test3() {
        //字符串转ByteBuffer
        String s = "hello";
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(s.getBytes());
        buffer.flip();
        log.info("buffer：{}", buffer.array());
        //由此可见调用array方法后position不发生改变
        log.info("buffer执行array方法后position位置：{}", buffer.position());

        //方法二
        ByteBuffer buffer2 = StandardCharsets.UTF_8.encode(s);
        buffer2.flip();
        log.info("buffer2：{}", buffer2.array());

        //方法三
        ByteBuffer buffer3 = ByteBuffer.wrap(s.getBytes());
        buffer3.flip();
        log.info("buffer3：{}", buffer3.array());

        //ByteBuffer转字符串
        String s1 = StandardCharsets.UTF_8.decode(buffer).toString();
        log.info("s1：{}", s1);

        byte[] array = buffer.array();
        log.info("s2：{}", new String(array));
    }

    /**
     * Scattering Reads 分散读取
     */
    @Test
    public void test4() {
        ByteBuffer buffer = ByteBuffer.allocate(3);
        ByteBuffer buffer2 = ByteBuffer.allocate(3);
        ByteBuffer buffer3 = ByteBuffer.allocate(5);
        try (RandomAccessFile randomAccessFile = new RandomAccessFile("file/words.txt", "r")) {
            try (FileChannel channel = randomAccessFile.getChannel()) {
                channel.read(new ByteBuffer[]{buffer, buffer2, buffer3});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        buffer.flip();
        buffer2.flip();
        buffer3.flip();
        log.info("buffer:{},buffer2:{},buffer3{}", new String(buffer.array()), new String(buffer2.array()), new String(buffer3.array()));
    }

    /**
     * Gathering Writes 集中写入，ByteBuffer显式的申请空间才可以写入
     */
    @Test
    public void test5() {
//        ByteBuffer buffer = StandardCharsets.UTF_8.encode("one");
//        ByteBuffer buffer2 = StandardCharsets.UTF_8.encode("two");
//        ByteBuffer buffer3 = StandardCharsets.UTF_8.encode("three");
//        buffer.flip();
//        buffer2.flip();
//        buffer3.flip();
        ByteBuffer buffer = ByteBuffer.allocate(3);
        ByteBuffer buffer2 = ByteBuffer.allocate(3);
        ByteBuffer buffer3 = ByteBuffer.allocate(5);
        buffer.put("one".getBytes());
        buffer2.put("two".getBytes());
        buffer3.put("three".getBytes());
        buffer.flip();
        buffer2.flip();
        buffer3.flip();
        log.info("buffer:{},buffer2:{},buffer3{}", new String(buffer.array()), new String(buffer2.array()), new String(buffer3.array()));
        try (RandomAccessFile randomAccessFile = new RandomAccessFile("file/words2.txt", "rw")) {
            //设置流的文件指针可以设置为追加写
            randomAccessFile.seek(randomAccessFile.length());
            try (FileChannel channel = randomAccessFile.getChannel()) {
                channel.write(new ByteBuffer[]{buffer, buffer2, buffer3});
                buffer.rewind();
                buffer2.rewind();
                buffer3.rewind();
                channel.write(new ByteBuffer[]{buffer, buffer2, buffer3});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 模拟服务端接收数据，数据可能发生粘包，半包。
     */
    @Test
    public void test6() {
        ByteBuffer source = ByteBuffer.allocate(32);
        source.put("Hello,world\nI'm zhangsan\nHo".getBytes());
        splitDate(source);
        source.put("w are you?\nhaha!\n".getBytes());
        splitDate(source);
    }

    public static void splitDate(ByteBuffer buffer) {
        //数据可能是Hello,world\nI'm zhangsan\nHo    w are you?\nhaha!\n发生粘包半包情况，分隔符为\n
        buffer.flip();
        for (int i = 0; i < buffer.limit(); i++) {
            if (buffer.get(i) == '\n') {
                int length = i + 1 - buffer.position();
                ByteBuffer target = ByteBuffer.allocate(length);
                byte[] data = new byte[length];
                buffer.get(data);
                target.put(data);
                ByteBufferUtil.debugAll(target);
            }
        }
//        int dateLen = buffer.limit();
        buffer.compact();
//        if (buffer.position() == dateLen) {
//            throw new RuntimeException("data too long,plaeas expansion!");
//        }
    }

    /**
     * channel的transferTo方法，将一个文件的数据拷贝到另一个文件，效率高，底层使用操作系统的零拷贝优化
     */
    @Test
    public void test7() {
        try (FileInputStream fileInputStream = new FileInputStream("file/aaa.txt");
             FileOutputStream fileOutputStream = new FileOutputStream("file/aaabak.txt")
        ) {
            FileChannel in = fileInputStream.getChannel();
            FileChannel out = fileOutputStream.getChannel();
//            out.transferFrom(in,0,in.size());
            //最多一次传输2g的数据
            long size = in.size();
            long left = size;
            while (left > 0) {
                left -= in.transferTo(size - left, left, out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件编程，Files类，Path，Paths类，FileChannel类
     */
    @Test
    public void test8() throws IOException {
        System.out.println(System.getProperty("user.dir"));
        //windows下不指定盘符则为相对路径，使用环境变量user.dir定位，user.dir指当前项目的根路径，在此即为E:\Example\Java\netty
        Path path = Paths.get("file/abc.txt");
        Path absolutePath = path.toAbsolutePath();
        log.info("file/abc.txt的绝对路径为：{}", absolutePath);

        //既可以表示相对路径又可以表示绝对路径，分隔符既可以使用\\也可以使用/，内部会自动转换
        Path path2 = Paths.get("E:\\Example\\Java\\netty\\file\\aaa.txt");
        Path path3 = Paths.get("E:/Example/Java/netty/file/aaabak.txt");
        //可以分开指定，可以表示目录或文件
        Path path4 = Paths.get("E:\\Example\\Java", "netty\\file");

        //.代表当前路径  ..代表上一级路径
        Path path5 = Paths.get("E:\\Example\\Java\\netty\\file\\aaa.txt\\..\\abc.txt");
        System.out.println("path2：" + path2);
        System.out.println("path3：" + path3);
        System.out.println("path4：" + path4);
        System.out.println("path5：" + path5);
        //对于path5可以调用normalize方法获取其正常的路径，也就是将.或..转换
        System.out.println("path5_normalize：" + path5.normalize());

        //Files检查Path指定的文件是否存在
        boolean pathExist = Files.exists(path);
        System.out.println("pathExist：" + pathExist);

        //创建一级目录，不能递归创建多级目录,如果文件或目录已存在或一次创建多个目录会抛异常
        Path createDirectoryPath = Paths.get("file/testPath");
        if (Files.notExists(createDirectoryPath)) {
            Path directory = Files.createDirectory(createDirectoryPath);
            System.out.println("createDirectory:file/testPath：" + directory.toAbsolutePath());
        }

        //创建多级目录，目录或文件已存在也不会抛异常
        Path directories = Files.createDirectories(Paths.get("file/testpath2/testPath3/testPath4"));
        System.out.println("createDirectories:file/testpath2/testPath3/testPath4：" + directories.toAbsolutePath());

        //拷贝文件  若目标文件已经存在会抛异常，如需覆目标文件需要指定拷贝选项
        Path from = Paths.get("file/aaa.txt");
        Path to = Paths.get("/file/aaabak.txt");
//        Files.copy(from,to);
        Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);

        //移动文件  StandardCopyOption.ATOMIC_MOVE 保证文件移动的原子性
        Path source = Paths.get("file/words.txt");
        Path target = Paths.get("file/testPath/words.txt");
        Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
    }

    /**
     * 遍历目录文件
     */
    @Test
    public void test9() throws IOException {
        //指定开始遍历的一级目录
        Path path = Paths.get("");
        //记录目录数和文件数
        AtomicInteger pathCount = new AtomicInteger();
        AtomicInteger fileCount = new AtomicInteger();
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                pathCount.incrementAndGet();
                System.out.println("dir：" + dir.toAbsolutePath());
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                fileCount.incrementAndGet();
                System.out.println("file：" + file.toAbsolutePath());
                return super.visitFile(file, attrs);
            }
        });
        System.out.println("pathCount：" + pathCount);
        System.out.println("fileCount：" + fileCount);
    }

    /**
     * 统计jdk目录下jar的数量
     */
    @Test
    public void test10() throws IOException {
        Path jdkPath = Paths.get("E:\\JavaSE");
        AtomicInteger jarCount = new AtomicInteger();
        Files.walkFileTree(jdkPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toFile().getName().endsWith(".jar")) {
                    jarCount.incrementAndGet();
                }
                return super.visitFile(file, attrs);
            }
        });
        System.out.println("jdk目录：" + jdkPath.toAbsolutePath() + "下的jar包数量为：" + jarCount.get());
    }

    /**
     * 删除多级目录
     */
    @Test
    public void test11() throws IOException {
        //指定要删除目录的一级目录
        Path path = Paths.get("file/testPath2");
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return super.postVisitDirectory(dir, exc);
            }
        });
    }

    /**
     * 拷贝多级目录
     */
    @Test
    public void test12() throws IOException {
        long start = System.currentTimeMillis();
        Path source = Paths.get("E:\\JavaSE");
        Path target = Paths.get("E:\\JavaSEbak");
        try {
            Files.walk(source).forEach(path -> {
                //目的是将根目录替换为新的根目录 将之后的目录中根目录部分替换为目标根目录，例如E:\JavaSE\bin -> E:\JavaSEbak\bin ,需要注意例如E:\JavaSE\xxx\JavaSE的特殊情况，所以使用replaceFirst不使用replace
                String targetName = path.toString().replaceFirst(source.toString(), target.toString());
                if (Files.isDirectory(path)) {
                    try {
                        Files.createDirectory(Paths.get(targetName));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (Files.isRegularFile(path)) {
                    try {
                        Files.copy(path, Paths.get(targetName));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println("拷贝耗时(s)：" + (end - start) / 1000);
    }

}





























