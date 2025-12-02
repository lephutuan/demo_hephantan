package services;

import models.UploadResult;
import models.TorrentFile;
import p2p.P2PPeer;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;

public class FileUploadService {

    private final String UPLOAD_DIR = "uploads/";
    private final String TORRENT_DIR = "torrents/";

    private final double SERVER_BANDWIDTH_MBPS = 300.0;

    public FileUploadService() {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
            Files.createDirectories(Paths.get(TORRENT_DIR));
        } catch (IOException e) {
            System.err.println("Tao thu muc that bai: " + e.getMessage());
        }
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

        String message = "Upload P2P thanh cong, chia thành " + torrent.getNumChunks() + " chunk, đang seed (peers: " + numPeers + ")";
        return new UploadResult(true, message, file.length(), speedMbps);
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
}
