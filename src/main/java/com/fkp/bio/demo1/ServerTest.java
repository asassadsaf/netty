package com.fkp.bio.demo1;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class ServerTest {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(4444);
            while (true){
                Socket socket = serverSocket.accept();
                new Thread(() -> {
                    try {
                        BufferedInputStream byteArrayInputStream = new BufferedInputStream(socket.getInputStream());
                        byte[] buffer = new byte[1000];
                        int offset = 0;
                        int readLen = 0;
                        while ((readLen = byteArrayInputStream.read(buffer, offset, buffer.length - offset)) != -1){
                            offset += readLen;
                            System.out.println(readLen);
                            System.out.println(Arrays.toString(buffer));
                        }
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
