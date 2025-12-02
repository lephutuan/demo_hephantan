package p2p;

import models.PeerInfo;
import java.util.*;
import java.util.concurrent.*;

public class PeerDiscovery {

    private final Tracker tracker;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<String, ScheduledFuture<?>> refreshTasks = new ConcurrentHashMap<>();
    
    public PeerDiscovery(Tracker tracker) {
        this.tracker = tracker;
    }

    public List<PeerInfo> discoverPeers(String torrentHash) {
        List<PeerInfo> peers = tracker.getPeersForTorrent(torrentHash);
        System.out.println("Discovered " + peers.size() + " peers for torrent: " + torrentHash);
        return peers;
    }

    public void announcePeer(String torrentHash, PeerInfo peerInfo) {
        tracker.registerPeer(torrentHash, peerInfo);
    }

    public void announcePeerWithChunks(String torrentHash, PeerInfo peerInfo, Set<Integer> availableChunks) {
        tracker.registerPeer(torrentHash, peerInfo);
        tracker.updatePeerChunks(torrentHash, peerInfo, availableChunks);
    }

    public void startPeriodicRefresh(String torrentHash, PeerInfo localPeer, Set<Integer> availableChunks, long intervalSeconds) {
        ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(() -> {
            try {
                tracker.registerPeer(torrentHash, localPeer);
                tracker.updatePeerChunks(torrentHash, localPeer, availableChunks);
                System.out.println("Refreshed peer announcement for torrent: " + torrentHash);
            } catch (Exception e) {
                System.err.println("Loi refresh peer: " + e.getMessage());
            }
        }, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
        
        refreshTasks.put(torrentHash, task);
    }

    public void stopPeriodicRefresh(String torrentHash) {
        ScheduledFuture<?> task = refreshTasks.remove(torrentHash);
        if (task != null) {
            task.cancel(false);
        }
    }

    public void leaveTorrent(String torrentHash, PeerInfo peerInfo) {
        stopPeriodicRefresh(torrentHash);
        tracker.unregisterPeer(torrentHash, peerInfo);
    }

    public void shutdown() {
        for (ScheduledFuture<?> task : refreshTasks.values()) {
            task.cancel(false);
        }
        refreshTasks.clear();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public int getPeerCount(String torrentHash) {
        return tracker.getPeerCount(torrentHash);
    }
}
