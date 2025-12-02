package models;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TorrentFile {

    private final String hash;
    private final String fileName;
    private final long fileSize;
    private final LocalDateTime createdAt;
    private final List<Chunk> chunks = new ArrayList<>();

    public TorrentFile(String hash, String fileName, long fileSize, int numberOfChunks) throws IOException {
        this.hash = hash;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.createdAt = LocalDateTime.now();

        File uploadsFile = new File("uploads/" + fileName);
        if (!uploadsFile.exists()) {
            throw new FileNotFoundException("Khong tim thay file ở uploads/: " + uploadsFile.getAbsolutePath());
        }

        splitFileIntoChunks(uploadsFile, numberOfChunks);
    }

    private void splitFileIntoChunks(File file, int numberOfChunks) throws IOException {
        if (numberOfChunks <= 0) {
            numberOfChunks = 1;
        }

        long fileLen = file.length();
        long chunkSize = (long) Math.ceil((double) fileLen / numberOfChunks);

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[(int) Math.min(chunkSize, Integer.MAX_VALUE)];
            int bytesRead;
            int index = 0;

            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] chunkData = Arrays.copyOf(buffer, bytesRead);
                chunks.add(new Chunk(index++, chunkData));
            }
        }
    }

    public List<Chunk> getChunks() {
        return Collections.unmodifiableList(chunks);
    }

    public int getNumChunks() {
        return chunks.size();
    }

    public void saveTo(File file) throws IOException {
        Properties props = new Properties();
        props.setProperty("hash", hash);
        props.setProperty("fileName", fileName);
        props.setProperty("fileSize", String.valueOf(fileSize));
        props.setProperty("createdAt", createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        props.setProperty("numChunks", String.valueOf(getNumChunks()));

        try (FileOutputStream fos = new FileOutputStream(file)) {
            props.store(fos, "Torrent file for " + fileName);
        }
    }

    public String getHash() {
        return hash;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
