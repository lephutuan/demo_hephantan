package models;

public class SpeedComparisonResult {

    private UploadResult clientServerResult;
    private UploadResult p2pResult;
    private double speedImprovement;
    private String recommendation;
    private long elapsedTime;
    private int activePeers;

    public SpeedComparisonResult(UploadResult clientServerResult, UploadResult p2pResult,
            double speedImprovement, String recommendation,
            long elapsedTime, int activePeers) {
        this.clientServerResult = clientServerResult;
        this.p2pResult = p2pResult;
        this.speedImprovement = speedImprovement;
        this.recommendation = recommendation;
        this.elapsedTime = elapsedTime;
        this.activePeers = activePeers;
    }

    public UploadResult getClientServerResult() {
        return clientServerResult;
    }

    public UploadResult getP2pResult() {
        return p2pResult;
    }

    public double getSpeedImprovement() {
        return speedImprovement;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public int getActivePeers() {
        return activePeers;
    }
}
