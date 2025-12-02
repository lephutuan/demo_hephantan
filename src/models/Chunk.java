package models;

public class Chunk {

    private final int id;
    private final byte[] data;
    private boolean downloaded;
    private String hash;

    public Chunk(int id, byte[] data) {
        this.id = id;
        this.data = data;
        this.downloaded = false;
        this.hash = null;
    }

    public Chunk(int id, byte[] data, String hash) {
        this.id = id;
        this.data = data;
        this.downloaded = false;
        this.hash = hash;
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

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
