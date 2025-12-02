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
        activePeers.add(new PeerInfo("127.0.0.1", 8081, "Peer-1"));
        activePeers.add(new PeerInfo("127.0.0.1", 8082, "Peer-2"));
        activePeers.add(new PeerInfo("127.0.0.1", 8083, "Peer-3"));
        activePeers.add(new PeerInfo("127.0.0.1", 8084, "Peer-4"));
        activePeers.add(new PeerInfo("127.0.0.1", 8085, "Peer-5"));
        activePeers.add(new PeerInfo("127.0.0.1", 8086, "Peer-6"));
        activePeers.add(new PeerInfo("127.0.0.1", 8087, "Peer-7"));
        activePeers.add(new PeerInfo("127.0.0.1", 8088, "Peer-8"));
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
