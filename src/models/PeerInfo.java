package models;

import java.time.LocalDateTime;

public class PeerInfo {

    private String ipAddress;
    private int port;
    private String peerId;
    private LocalDateTime lastSeen;

    public PeerInfo(String ipAddress, int port, String peerId) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.peerId = peerId;
        this.lastSeen = LocalDateTime.now();
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public String getPeerId() {
        return peerId;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }
}
