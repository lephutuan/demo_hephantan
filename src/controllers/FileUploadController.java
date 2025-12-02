package controllers;

import models.UploadResult;
import models.SpeedComparisonResult;
import models.PeerInfo;
import services.FileUploadService;
import services.SpeedComparisonService;

import java.io.File;
import java.util.List;

public class FileUploadController {

    private final FileUploadService fileUploadService;
    private final SpeedComparisonService speedComparisonService;

    public FileUploadController(FileUploadService fileUploadService, SpeedComparisonService speedComparisonService) {
        this.fileUploadService = fileUploadService;
        this.speedComparisonService = speedComparisonService;
    }

    public FileUploadService getFileUploadService() {
        return fileUploadService;
    }

    public SpeedComparisonService getSpeedComparisonService() {
        return speedComparisonService;
    }


    public UploadResult uploadP2P(File file, int numChunks) throws Exception {
        return fileUploadService.uploadP2P(file, numChunks);
    }

    public UploadResult uploadClientServer(File file) throws Exception {
        return fileUploadService.uploadClientServer(file);
    }

    public SpeedComparisonResult compareSpeeds(File file, int numChunks) throws Exception {
        return speedComparisonService.compareUploadSpeeds(file, numChunks);
    }

    public List<PeerInfo> getActivePeers() {
        return speedComparisonService.getActivePeers();
    }
}
