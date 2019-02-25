package com.company;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

public class TCPConnection {
    private ServerSocket server;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;


    //client contructor
    TCPConnection(String ipHost, int port) {
        try {
            socket = new Socket(InetAddress.getByName(ipHost), port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //server constructor
    TCPConnection(int port) {
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void listenForСonnection() {
        try {
            socket = server.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCommunication() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String listenMsg() {
        String msg = null;
        try {
            msg = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("TCP get a message from tunnel");
        System.out.println(msg);
        return msg;
    }

    public void sendMSg(String msg) {
        System.out.println("TCP is going to send data over tunnel ");
        System.out.println(msg);
        writer.println(msg);
    }


    //method for sending initial message with config data from client to server
    public void sendInitialMSG(String ipTarget, String[] targetPorts) {
        StringBuilder builder = new StringBuilder();
        builder.append(ipTarget).append(" ");
        Arrays.stream(targetPorts).forEach(i -> builder.append(i).append(" "));
        sendMSg(builder.toString());
    }

    public String getServerIP() {
        String ip = "";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return ip;
    }

    public int getServerPort() {
        return server.getLocalPort();
    }

    public String getClientIP() {
        return socket.getInetAddress().getHostAddress();
    }

    public int getClientPort() {
        return socket.getPort();
    }

}
