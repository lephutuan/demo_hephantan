package models;

public class Chunk {

    private final int id;
    private final byte[] data;
    private boolean downloaded;

    public Chunk(int id, byte[] data) {
        this.id = id;
        this.data = data;
        this.downloaded = false;
    }

    public int getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }
}
