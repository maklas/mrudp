package ru.maklas.mrudp;

import java.net.InetAddress;

public interface ServerModel {

    byte[] validateNewConnection(InetAddress address, int port, byte[] userData);

    void registerNewConnection(MRUDPSocketImpl socket);

    void handleUnknownSourceMsg(byte[] userData);

    void onSocketDisconnected(MRUDPSocketImpl socket);

}