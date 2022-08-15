package com.fkp.netty.param;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class BacklogBioClient {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("localhost",9000));
        socket.getOutputStream().write(1);
        System.in.read();

    }


}
