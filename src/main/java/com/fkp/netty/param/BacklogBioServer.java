package com.fkp.netty.param;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BacklogBioServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9000,2);
        Socket accept = serverSocket.accept();
        System.out.println(accept);
        System.in.read();
    }
}
