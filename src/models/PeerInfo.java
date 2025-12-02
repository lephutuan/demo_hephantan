package models;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PeerInfo {

    private String ipAddress;
    private int port;
    private String peerId;
    private LocalDateTime lastSeen;
    private long latency;
    private double bandwidth;
    private Set<Integer> availableChunks;
    private int currentLoad;

    public PeerInfo(String ipAddress, int port, String peerId) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.peerId = peerId;
        this.lastSeen = LocalDateTime.now();
        this.latency = 0;
        this.bandwidth = 0.0;
        this.availableChunks = ConcurrentHashMap.newKeySet();
        this.currentLoad = 0;
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

    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }

    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }

    public double getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(double bandwidth) {
        this.bandwidth = bandwidth;
    }

    public Set<Integer> getAvailableChunks() {
        return availableChunks;
    }

    public void setAvailableChunks(Set<Integer> availableChunks) {
        this.availableChunks = availableChunks;
    }

    public void addAvailableChunk(int chunkId) {
        this.availableChunks.add(chunkId);
    }

    public boolean hasChunk(int chunkId) {
        return this.availableChunks.contains(chunkId);
    }

    public int getCurrentLoad() {
        return currentLoad;
    }

    public void setCurrentLoad(int currentLoad) {
        this.currentLoad = currentLoad;
    }

    public void incrementLoad() {
        this.currentLoad++;
    }

    public void decrementLoad() {
        if (this.currentLoad > 0) {
            this.currentLoad--;
        }
    }
}
