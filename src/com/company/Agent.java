package com.company;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Agent {
    private static TCPConnection tcpTunnel;
    private static List<UDPController> udpCommuniaction = new ArrayList<>();
    private static ExecutorService udpPool = Executors.newCachedThreadPool();
    private static ExecutorService tcpPool = Executors.newCachedThreadPool();
    private static Scanner userInput = new Scanner(System.in);//need for future extensions which will include command dictionary
    private static BlockingQueue<String> fromUDPdataBUffer = new LinkedBlockingQueue<>();

    private Agent(String[] args) {
        tcpTunnel = new TCPConnection(args[0], Integer.parseInt(args[1]));
        tcpTunnel.setCommunication();
        System.out.println("TCP tunnel with " + args[0] + " " + args[1] + " established");
        tcpTunnel.sendInitialMSG(args[2], Arrays.copyOfRange(args, 3, args.length));
        System.out.println("TCP tunnel configuered successfully");
        System.out.println("Open UDP ports...");
        startUDPs(args);
        startWorkWithUDP();
        startWorkWithTCPTunnel();
    }


    public static void main(String[] args) {
        new Agent(args);
    }

    //sending and receiving data from tunnel
    public static void startWorkWithTCPTunnel() {
        tcpPool.submit(() -> {
            while (true) {
                String msg = null;
                try {
                    msg = fromUDPdataBUffer.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                tcpTunnel.sendMSg(msg);
            }
        });

        tcpPool.submit(() -> {
            try {
                while (true) {
                    String msg = tcpTunnel.listenMsg();
                    int port = extractPortFromMSG(msg);
                    System.out.println(msg);
                    System.out.println(port);
                    udpCommuniaction.stream().filter(i -> i.getBoundPort() == port).forEach(i -> i.sendDataToClient(msg));
                }
            } catch (IOException E) {
                System.out.println("Connection with repeater lost");
            }
        });
    }

    //creating a content of udp controllers list which allowed to communiaction with clients
    public static void startUDPs(String[] args) {
        Arrays.stream(Arrays.copyOfRange(args, 3, args.length)).mapToInt(Integer::parseInt).forEach(i -> {
            try {
                udpCommuniaction.add(UDPController.create(i));
                System.out.println("UDP port " + i + " opened");
            } catch (SocketException e) {
                System.out.println("Wrong port " + i);
            }
        });
    }

    //listen for initial msg and after that listen messages from that  client
    public static void startWorkWithUDP() {
        udpCommuniaction.forEach(i -> {
            udpPool.submit(() -> {
                i.listenForInitialMsg(fromUDPdataBUffer);
                while (true) {
                    i.listenFromClient(fromUDPdataBUffer);
                }
            });
        });
    }


    public static int extractPortFromMSG(String msg) {
        String[] fragments = msg.split("\\s");
        return Integer.parseInt(fragments[fragments.length - 1]);
    }

}
