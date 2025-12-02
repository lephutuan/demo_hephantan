package p2p;

import models.PeerInfo;
import java.util.*;
import java.util.stream.Collectors;

public class PeerSelector {

    private static final double LATENCY_WEIGHT = 0.3;
    private static final double BANDWIDTH_WEIGHT = 0.4;
    private static final double LOAD_WEIGHT = 0.3;
    
    private static final double MAX_ACCEPTABLE_LATENCY_MS = 1000.0;
    private static final double MAX_BANDWIDTH_MBPS = 1000.0;
    private static final int MAX_PEER_LOAD = 10;

    public List<PeerInfo> selectOptimalPeers(List<PeerInfo> availablePeers, int chunkId, int maxPeers) {
        if (availablePeers == null || availablePeers.isEmpty()) {
            return new ArrayList<>();
        }

        List<PeerInfo> peersWithChunk = availablePeers.stream()
                .filter(peer -> peer.hasChunk(chunkId))
                .collect(Collectors.toList());

        if (peersWithChunk.isEmpty()) {
            return new ArrayList<>();
        }

        List<PeerInfo> rankedPeers = rankPeers(peersWithChunk);
        
        return rankedPeers.stream()
                .limit(maxPeers)
                .collect(Collectors.toList());
    }

    public PeerInfo selectBestPeer(List<PeerInfo> availablePeers, int chunkId) {
        List<PeerInfo> optimal = selectOptimalPeers(availablePeers, chunkId, 1);
        return optimal.isEmpty() ? null : optimal.get(0);
    }

    private List<PeerInfo> rankPeers(List<PeerInfo> peers) {
        return peers.stream()
                .sorted((p1, p2) -> Double.compare(calculateScore(p2), calculateScore(p1)))
                .collect(Collectors.toList());
    }

    private double calculateScore(PeerInfo peer) {
        double latencyScore = calculateLatencyScore(peer.getLatency());
        double bandwidthScore = calculateBandwidthScore(peer.getBandwidth());
        double loadScore = calculateLoadScore(peer.getCurrentLoad());

        return (latencyScore * LATENCY_WEIGHT) +
               (bandwidthScore * BANDWIDTH_WEIGHT) +
               (loadScore * LOAD_WEIGHT);
    }

    private double calculateLatencyScore(long latency) {
        if (latency <= 0) {
            return 1.0;
        }
        
        if (latency >= MAX_ACCEPTABLE_LATENCY_MS) {
            return 0.0;
        }
        
        return 1.0 - (latency / MAX_ACCEPTABLE_LATENCY_MS);
    }

    private double calculateBandwidthScore(double bandwidth) {
        if (bandwidth <= 0) {
            return 0.5;
        }
        
        return Math.min(bandwidth / MAX_BANDWIDTH_MBPS, 1.0);
    }

    private double calculateLoadScore(int currentLoad) {
        if (currentLoad >= MAX_PEER_LOAD) {
            return 0.0;
        }
        
        return 1.0 - ((double) currentLoad / MAX_PEER_LOAD);
    }

    public List<PeerInfo> selectPeersRoundRobin(List<PeerInfo> availablePeers, List<Integer> chunkIds) {
        if (availablePeers == null || availablePeers.isEmpty() || chunkIds == null || chunkIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<PeerInfo> selectedPeers = new ArrayList<>();
        int peerIndex = 0;

        for (Integer chunkId : chunkIds) {
            List<PeerInfo> peersWithChunk = availablePeers.stream()
                    .filter(peer -> peer.hasChunk(chunkId))
                    .collect(Collectors.toList());

            if (!peersWithChunk.isEmpty()) {
                PeerInfo selectedPeer = peersWithChunk.get(peerIndex % peersWithChunk.size());
                selectedPeers.add(selectedPeer);
                peerIndex++;
            }
        }

        return selectedPeers;
    }

    public Map<Integer, PeerInfo> distributeChunksToPeers(List<PeerInfo> availablePeers, List<Integer> chunkIds) {
        Map<Integer, PeerInfo> distribution = new HashMap<>();

        for (Integer chunkId : chunkIds) {
            PeerInfo bestPeer = selectBestPeer(availablePeers, chunkId);
            if (bestPeer != null) {
                distribution.put(chunkId, bestPeer);
            }
        }

        return distribution;
    }
}
