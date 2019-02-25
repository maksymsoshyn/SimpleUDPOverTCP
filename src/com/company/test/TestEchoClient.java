package com.company.test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class TestEchoClient {
    private static DatagramSocket socket;
    private static DatagramPacket packetR;

    public static void main(String[] args) throws IOException {
        socket = new DatagramSocket(Integer.parseInt(args[1]));
        packetR = new DatagramPacket(new byte[1024], 0, 1024);
        Scanner input = new Scanner(System.in);

        while (true) {
            byte[] msg = input.nextLine().getBytes();
            socket.send(new DatagramPacket(msg, 0, msg.length, InetAddress.getByName(args[0]), Integer.parseInt(args[2])));
            socket.receive(packetR);
            String answer = new String(packetR.getData(), packetR.getOffset(), packetR.getLength());
            System.out.println(answer);
        }

    }
}
