package services;

import models.UploadResult;
import models.TorrentFile;
import models.PeerInfo;
import models.Chunk;
import p2p.*;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

public class FileUploadService {

    private final String UPLOAD_DIR = "uploads/";
    private final String TORRENT_DIR = "torrents/";

    private final double SERVER_BANDWIDTH_MBPS = 300.0;
    
    private final Tracker tracker;
    private final PeerDiscovery peerDiscovery;
    private final ChunkDownloader chunkDownloader;

    public FileUploadService() {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
            Files.createDirectories(Paths.get(TORRENT_DIR));
        } catch (IOException e) {
            System.err.println("Tao thu muc that bai: " + e.getMessage());
        }
        
        this.tracker = new Tracker();
        this.peerDiscovery = new PeerDiscovery(tracker);
        this.chunkDownloader = new ChunkDownloader(5);
    }

    public String getUPLOAD_DIR() {
        return UPLOAD_DIR;
    }

    public String getTORRENT_DIR() {
        return TORRENT_DIR;
    }

    public UploadResult uploadClientServer(File file) throws Exception {
        long start = System.currentTimeMillis();

        Path dest = Paths.get(UPLOAD_DIR + file.getName());
        Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

        long end = System.currentTimeMillis();

        double transferSeconds = (file.length() * 8.0) / (SERVER_BANDWIDTH_MBPS * 1_000_000.0);
        long simulatedMs = (long) (transferSeconds * 1000);
        if (simulatedMs > (end - start)) {
            Thread.sleep(simulatedMs - (end - start));
            end = System.currentTimeMillis();
        }

        double speed = calculateSpeed(file.length(), end - start);
        return new UploadResult(true, "Upload Client-Server thanh cong", file.length(), speed);
    }

    public UploadResult uploadP2P(File file, int numChunks) throws Exception {
        final int numPeers = 5;
        final double peerBandwidthMbps = 100.0;

        long start = System.currentTimeMillis();
        double totalBandwidth = numPeers * peerBandwidthMbps;
        double transferSeconds = (file.length() * 8.0) / (totalBandwidth * 1_000_000.0);
        long simulatedMs = (long) (transferSeconds * 1000);

        Thread.sleep(simulatedMs);

        long end = System.currentTimeMillis();
        double speedMbps = calculateSpeed(file.length(), end - start);

        TorrentFile torrent = createTorrentFile(file, numChunks);

        P2PPeer peer = new P2PPeer(8080);
        peer.startSeeding(torrent);
        
        PeerInfo localPeerInfo = new PeerInfo("127.0.0.1", 8080, "Seed-Peer");
        Set<Integer> allChunks = new HashSet<>();
        for (int i = 0; i < torrent.getNumChunks(); i++) {
            allChunks.add(i);
        }
        peerDiscovery.announcePeerWithChunks(torrent.getHash(), localPeerInfo, allChunks);

        String message = "Upload P2P thanh cong, chia thành " + torrent.getNumChunks() + " chunk, đang seed (peers: " + numPeers + ")";
        return new UploadResult(true, message, file.length(), speedMbps);
    }
    
    public File downloadP2P(String torrentHash, String outputPath) throws Exception {
        List<PeerInfo> availablePeers = peerDiscovery.discoverPeers(torrentHash);
        
        if (availablePeers.isEmpty()) {
            throw new Exception("Khong tim thay peer nao cho torrent: " + torrentHash);
        }
        
        PeerInfo firstPeer = availablePeers.get(0);
        int numChunks = getNumChunksFromPeer(firstPeer, torrentHash);
        
        List<Integer> chunkIds = new ArrayList<>();
        for (int i = 0; i < numChunks; i++) {
            chunkIds.add(i);
        }
        
        List<Chunk> downloadedChunks = chunkDownloader.downloadChunksParallel(availablePeers, chunkIds, torrentHash);
        
        if (downloadedChunks.size() != numChunks) {
            throw new Exception("Download khong day du: " + downloadedChunks.size() + "/" + numChunks + " chunks");
        }
        
        File outputFile = chunkDownloader.mergeChunks(downloadedChunks, outputPath);
        return outputFile;
    }
    
    private int getNumChunksFromPeer(PeerInfo peer, String torrentHash) throws IOException {
        return peer.getAvailableChunks().size();
    }

    private double calculateSpeed(long bytes, long milliseconds) {
        if (milliseconds <= 0) {
            return 0;
        }
        double seconds = milliseconds / 1000.0;
        double megabits = (bytes * 8.0) / 1_000_000.0;
        return megabits / seconds;
    }

    private TorrentFile createTorrentFile(File file, int numChunks) throws Exception {
        String hash = calculateFileHash(file);
        TorrentFile torrent = new TorrentFile(hash, file.getName(), file.length(), numChunks);
        torrent.saveTo(Paths.get(TORRENT_DIR + file.getName() + ".torrent").toFile());
        return torrent;
    }

    private String calculateFileHash(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                md.update(buffer, 0, read);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : md.digest()) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    public Tracker getTracker() {
        return tracker;
    }
    
    public PeerDiscovery getPeerDiscovery() {
        return peerDiscovery;
    }
    
    public ChunkDownloader getChunkDownloader() {
        return chunkDownloader;
    }
    
    public void shutdown() {
        if (chunkDownloader != null) {
            chunkDownloader.shutdown();
        }
        if (peerDiscovery != null) {
            peerDiscovery.shutdown();
        }
    }
}
