package ru.maklas.mrudp;

public interface MRUDPListener {

    void onDisconnect(MRUDPSocket fixedBufferMRUDP2);

    void onPingUpdated(int newPing);
}
