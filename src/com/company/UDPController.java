package com.company;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class UDPController {

    private byte[] receiveBuffer = new byte[1024];
    private DatagramSocket udpSocket;
    private DatagramPacket receivedPacket = new DatagramPacket(receiveBuffer, 0, receiveBuffer.length);
    private String boundIp = "";
    private int boundPort;
    private ClientInfo client;
    private List<Integer> destPorts = new LinkedList<>();
    private String destIp;


    private UDPController() {
    }


    //create and share interface for managing udp
    public static UDPController create(int port) throws SocketException {
        UDPController connection = new UDPController();
        connection.udpSocket = new DatagramSocket(port);
        connection.boundPort = port;
        connection.boundIp = connection.udpSocket.getLocalAddress().getHostAddress();
        return connection;
    }


    //waitinig from the first message from any host to make it client(for agent)
    public void listenForInitialMsg(BlockingQueue<String> bufferData) {
        String msg = listenMessage();
        client = new ClientInfo();
        client.setNameClient(receivedPacket.getAddress().getHostAddress());
        client.setPortClient(receivedPacket.getPort());
        client.setDestPort(boundPort);
        System.out.println("UDP got an initial message from " + client.toString());
        try {
            bufferData.put(msg + " " + boundPort);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    ////listening message and check, whether it come from client host or not
    public void listenFromClient(BlockingQueue<String> bufferData) {
        String msg = listenMessage();
        if (receivedPacket.getPort() == client.getPortClient() && receivedPacket.getAddress().getHostAddress().equals(client.getNameClient())) {
            System.out.println("UDP got a message from client " + client.toString());
            System.out.println(msg);
            try {
                bufferData.put(msg + " " + boundPort);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //listening message and check, whether it come from dest host or not
    public void listenFromDest(BlockingQueue<String> bufferData) {
        String msg = listenMessage();
        if (destPorts.contains(receivedPacket.getPort()) && destIp.equals(receivedPacket.getAddress().getHostAddress())) {
            try {
                System.out.println("UDP got a message from dest host");
                System.out.println(msg);
                bufferData.put(msg + " " + receivedPacket.getPort());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    //listening for any messages
    public String listenMessage() {
        try {
            udpSocket.receive(receivedPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(receivedPacket.getData(), receivedPacket.getOffset(), receivedPacket.getLength());
    }


    //sending data to client
    public void sendDataToClient(String msg) {
        try {
            byte[] sendData = extractPureMSG(msg).getBytes();

            System.out.println("UDP is going to send data to client");
            System.out.println(extractPureMSG(msg));
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(client.getNameClient()), client.getPortClient());
            udpSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //sending data to appropriate dest host
    public void sendDataToDest(String msg) {
        try {
            byte[] sendData = extractPureMSG(msg).getBytes();
            int port = extractPortFromMSG(msg);

            System.out.println("UDP is going to send data to dest");
            System.out.println(extractPureMSG(msg));
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(destIp), port);
            udpSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setDestPorts(List<Integer> ports) {
        destPorts = ports;
    }

    public void setDestIp(String destIp) {
        this.destIp = destIp;
    }

    //return info about socket to which UDP was bound
    public String toString() {
        return boundIp + " " + boundPort;
    }


    public int extractPortFromMSG(String msg) {
        String[] fragments = msg.split("\\s");
        return Integer.parseInt(fragments[fragments.length - 1]);
    }

    public String extractPureMSG(String msg) {
        StringBuilder builder = new StringBuilder();
        String[] fragments = msg.split("\\s");
        builder.append(fragments[0]);
        if (fragments.length > 1)
            Arrays.stream(Arrays.copyOfRange(fragments, 1, fragments.length - 1)).forEach(i -> builder.append(" ").append(i));
        return builder.toString();
    }

    public int getDestPort() {
        return receivedPacket.getPort();
    }

    public String getDestIp() {
        return receivedPacket.getAddress().getHostAddress();
    }


    public int getBoundPort() {
        return boundPort;
    }
}
