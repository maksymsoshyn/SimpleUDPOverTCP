package com.company;

public class ClientInfo {
    private int portClient;
    private int destPort;
    private String nameClient;
    private String destName;


    ClientInfo(String name) {
        nameClient = name;
    }

    ClientInfo() {

    }

    public void setNameClient(String newName) {
        nameClient = newName;
    }

    public void setDestName(String name) {
        destName = name;
    }

    public void setPortClient(int newPort) {
        portClient = newPort;
    }

    public void setDestPort(int destPort) {
        this.destPort = destPort;
    }

    public int getPortClient() {
        return portClient;
    }

    public String getNameClient() {
        return nameClient;
    }

    public int getDestPort() {
        return destPort;
    }

    public String getDestName() {
        return destName;
    }

    public String toString() {
        return nameClient + " " + portClient;
    }


}
