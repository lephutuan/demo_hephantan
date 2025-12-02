package models;

public class UploadResult {

    private boolean success;
    private String message;
    private long fileSize;
    private double speedMbps;

    public UploadResult(boolean success, String message, long fileSize, double speedMbps) {
        this.success = success;
        this.message = message;
        this.fileSize = fileSize;
        this.speedMbps = speedMbps;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public long getFileSize() {
        return fileSize;
    }

    public double getSpeedMbps() {
        return speedMbps;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setSpeedMbps(double speedMbps) {
        this.speedMbps = speedMbps;
    }
}
