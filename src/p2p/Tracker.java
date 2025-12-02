package p2p;

import models.PeerInfo;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;

public class Tracker {

    private final Map<String, Set<PeerInfo>> torrentPeers = new ConcurrentHashMap<>();
    private final Map<String, PeerInfo> allPeers = new ConcurrentHashMap<>();
    
    public synchronized void registerPeer(String torrentHash, PeerInfo peerInfo) {
        String peerKey = peerInfo.getIpAddress() + ":" + peerInfo.getPort();
        allPeers.put(peerKey, peerInfo);
        
        torrentPeers.computeIfAbsent(torrentHash, k -> ConcurrentHashMap.newKeySet()).add(peerInfo);
        peerInfo.setLastSeen(LocalDateTime.now());
        
        System.out.println("Peer registered: " + peerInfo.getPeerId() + " for torrent: " + torrentHash);
    }

    public synchronized void unregisterPeer(String torrentHash, PeerInfo peerInfo) {
        Set<PeerInfo> peers = torrentPeers.get(torrentHash);
        if (peers != null) {
            peers.remove(peerInfo);
            if (peers.isEmpty()) {
                torrentPeers.remove(torrentHash);
            }
        }
        
        String peerKey = peerInfo.getIpAddress() + ":" + peerInfo.getPort();
        allPeers.remove(peerKey);
        
        System.out.println("Peer unregistered: " + peerInfo.getPeerId() + " for torrent: " + torrentHash);
    }

    public List<PeerInfo> getPeersForTorrent(String torrentHash) {
        Set<PeerInfo> peers = torrentPeers.get(torrentHash);
        if (peers == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(peers);
    }

    public synchronized void updatePeerChunks(String torrentHash, PeerInfo peerInfo, Set<Integer> availableChunks) {
        Set<PeerInfo> peers = torrentPeers.get(torrentHash);
        if (peers != null) {
            for (PeerInfo peer : peers) {
                if (peer.getPeerId().equals(peerInfo.getPeerId())) {
                    peer.setAvailableChunks(availableChunks);
                    peer.setLastSeen(LocalDateTime.now());
                    break;
                }
            }
        }
    }

    public synchronized void cleanupStalePeers(int timeoutMinutes) {
        LocalDateTime now = LocalDateTime.now();
        List<String> torrentsToClean = new ArrayList<>();
        
        for (Map.Entry<String, Set<PeerInfo>> entry : torrentPeers.entrySet()) {
            Set<PeerInfo> peers = entry.getValue();
            peers.removeIf(peer -> {
                boolean isStale = peer.getLastSeen().plusMinutes(timeoutMinutes).isBefore(now);
                if (isStale) {
                    String peerKey = peer.getIpAddress() + ":" + peer.getPort();
                    allPeers.remove(peerKey);
                }
                return isStale;
            });
            
            if (peers.isEmpty()) {
                torrentsToClean.add(entry.getKey());
            }
        }
        
        for (String torrentHash : torrentsToClean) {
            torrentPeers.remove(torrentHash);
        }
    }

    public int getPeerCount(String torrentHash) {
        Set<PeerInfo> peers = torrentPeers.get(torrentHash);
        return peers == null ? 0 : peers.size();
    }

    public Map<String, Integer> getAllTorrentStats() {
        Map<String, Integer> stats = new HashMap<>();
        for (Map.Entry<String, Set<PeerInfo>> entry : torrentPeers.entrySet()) {
            stats.put(entry.getKey(), entry.getValue().size());
        }
        return stats;
    }
}
