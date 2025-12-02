package services;

import models.UploadResult;
import models.SpeedComparisonResult;
import models.PeerInfo;

import java.io.File;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class SpeedComparisonService {

    private final FileUploadService fileUploadService;
    private final List<PeerInfo> activePeers = new CopyOnWriteArrayList<>();

    public SpeedComparisonService(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
        initializePeers();
    }

    private void initializePeers() {
        PeerInfo peer1 = new PeerInfo("127.0.0.1", 8081, "Peer-1");
        peer1.setLatency(50);
        peer1.setBandwidth(100.0);
        activePeers.add(peer1);
        
        PeerInfo peer2 = new PeerInfo("127.0.0.1", 8082, "Peer-2");
        peer2.setLatency(30);
        peer2.setBandwidth(150.0);
        activePeers.add(peer2);
        
        PeerInfo peer3 = new PeerInfo("127.0.0.1", 8083, "Peer-3");
        peer3.setLatency(70);
        peer3.setBandwidth(80.0);
        activePeers.add(peer3);
        
        PeerInfo peer4 = new PeerInfo("127.0.0.1", 8084, "Peer-4");
        peer4.setLatency(40);
        peer4.setBandwidth(120.0);
        activePeers.add(peer4);
        
        PeerInfo peer5 = new PeerInfo("127.0.0.1", 8085, "Peer-5");
        peer5.setLatency(60);
        peer5.setBandwidth(90.0);
        activePeers.add(peer5);
        
        PeerInfo peer6 = new PeerInfo("127.0.0.1", 8086, "Peer-6");
        peer6.setLatency(35);
        peer6.setBandwidth(110.0);
        activePeers.add(peer6);
        
        PeerInfo peer7 = new PeerInfo("127.0.0.1", 8087, "Peer-7");
        peer7.setLatency(55);
        peer7.setBandwidth(95.0);
        activePeers.add(peer7);
        
        PeerInfo peer8 = new PeerInfo("127.0.0.1", 8088, "Peer-8");
        peer8.setLatency(45);
        peer8.setBandwidth(105.0);
        activePeers.add(peer8);
    }


    public SpeedComparisonResult compareUploadSpeeds(File file, int numChunks) throws Exception {
        long start = System.currentTimeMillis();

        UploadResult clientResult = fileUploadService.uploadClientServer(file);
        UploadResult p2pResult = fileUploadService.uploadP2P(file, numChunks);

        double speedDiff = p2pResult.getSpeedMbps() - clientResult.getSpeedMbps();
        double speedImprovement = clientResult.getSpeedMbps() > 0 ? (speedDiff / clientResult.getSpeedMbps()) * 100 : 0;

        String recommendation;
        if (speedImprovement > 20) {
            recommendation = "P2P nhanh hon nhieu";
        } else if (speedImprovement > 0) {
            recommendation = "P2P nhanh hơn mot chut";
        } else {
            recommendation = "Client-Server nhanh hon";
        }

        return new SpeedComparisonResult(
                clientResult, p2pResult,
                speedImprovement, recommendation,
                System.currentTimeMillis() - start,
                activePeers.size()
        );
    }

    public List<PeerInfo> getActivePeers() {
        return new ArrayList<>(activePeers);
    }
}
