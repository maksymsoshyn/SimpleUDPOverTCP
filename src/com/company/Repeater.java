package com.company;

import java.io.IOException;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Repeater {
    private static ExecutorService udpDestsPool = Executors.newCachedThreadPool();
    private static ExecutorService tcpTunnelPool = Executors.newCachedThreadPool();

    private Repeater(String args[]) {
        TCPConnection tcpTunnel = new TCPConnection(Integer.parseInt(args[0]));
        System.out.println("Your data for connection over TCP: ");
        System.out.println(tcpTunnel.getServerIP());
        System.out.println(tcpTunnel.getServerPort());
        BlockingQueue<String> configQueue = new LinkedBlockingQueue<>();//need for future multiagent extension
        startTunnel(tcpTunnel, configQueue);
        UDPController udpController = configureUDP(configQueue);
        BlockingQueue<String> fromUDPdataBUffer = new LinkedBlockingQueue<>();
        startWorkWithTunnel(tcpTunnel, udpController, fromUDPdataBUffer);
        startWorkWithUDP(udpController, fromUDPdataBUffer);
    }


    public static void main(String[] args) {
        new Repeater(args);
    }

    //listen for connection, setting communication, listen for config msg
    public static void startTunnel(TCPConnection tcpTunnel, BlockingQueue<String> configQueue) {
        try {
            tcpTunnel.listenForСonnection();
            System.out.println("Agent " + tcpTunnel.getClientIP() + " " + tcpTunnel.getClientPort() + " triyng to establish TCP tunnel");
            tcpTunnel.setCommunication();
            System.out.println("TCP tunnel established");
            System.out.println("Waiting for configuration...");
            configQueue.put(tcpTunnel.listenMsg());
            System.out.println("TCP tunnel configuered successfully");
        } catch (InterruptedException | IOException e) {
            System.out.println("Connection with agent lost");
        }
    }

    //create and configurate udpControoler
    public static UDPController configureUDP(BlockingQueue<String> configQUeue) {
        UDPController udpController;
        int port = (int) (Math.random() * 64514) + 1024;
        while (true) {
            try {
                udpController = UDPController.create(port);
                System.out.println("UDP port was opened");
                String initialData = configQUeue.take();
                udpController.setDestPorts(extractDestPorts(initialData));
                udpController.setDestIp(extractDestIp(initialData));
                break;
            } catch (SocketException | InterruptedException e) {
                port++;
            }
        }
        return udpController;
    }

    //listening data from tunnel and sending data over tunnel
    public static void startWorkWithTunnel(TCPConnection tcpTunnel, UDPController udpController, BlockingQueue<String> fromUDPdataBUffer) {
        tcpTunnelPool.submit(() -> {
            while (true) {
                String msg = tcpTunnel.listenMsg();
                udpController.sendDataToDest(msg);
            }
        });
        tcpTunnelPool.submit(() -> {
            while (true) {
                String msg = fromUDPdataBUffer.take();
                tcpTunnel.sendMSg(msg);
            }
        });
    }

    //listening data from destination ports and send it to data buffer
    public static void startWorkWithUDP(UDPController udpController, BlockingQueue<String> fromUDPdataBUffer) {
        udpDestsPool.submit(() -> {
            while (true) {
                udpController.listenFromDest(fromUDPdataBUffer);
            }
        });
    }

    /*Methods below need for seting configuration of udp*/


    private static List<Integer> extractDestPorts(String msg) {
        String[] fragments = msg.split("\\s");
        List<Integer> ports = new LinkedList<>();
        for (int i = 1; i < fragments.length; i++)
            ports.add(Integer.parseInt(fragments[i]));
        return ports;
    }

    private static String extractDestIp(String msg) {
        return msg.split("\\s")[0];
    }


}

