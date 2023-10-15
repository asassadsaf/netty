package com.fkp.bio.demo1;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class ClientTest {
    public static void main(String[] args) {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress("localhost",4444));
            OutputStream outputStream = socket.getOutputStream();
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()){
                String next = scanner.next();
                if("0".equals(next)){
                    break;
                }
                System.out.println(next);
                outputStream.write(next.getBytes());
                outputStream.flush();
            }
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
