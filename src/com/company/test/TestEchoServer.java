package com.company.test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class TestEchoServer {
    private static DatagramSocket listenHere;
    private static DatagramPacket request;


    public static void main(String[] args) throws IOException {
        listenHere = new DatagramSocket(Integer.parseInt(args[0]));
        request = new DatagramPacket(new byte[1024], 0, 1024);
        listenHere.receive(request);
        String answer = new String(request.getData(), 0, request.getLength()) + " echo";
        listenHere.send(new DatagramPacket(answer.getBytes(), answer.getBytes().length, request.getSocketAddress()));
        System.out.println("answered");

    }
}
