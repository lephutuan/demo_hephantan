package services;

import models.Chunk;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChunkVerifier {

    public static String calculateChunkHash(byte[] chunkData) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(chunkData);
        
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static boolean verifyChunk(Chunk chunk) {
        if (chunk == null || chunk.getData() == null || chunk.getHash() == null) {
            return false;
        }

        try {
            String calculatedHash = calculateChunkHash(chunk.getData());
            return calculatedHash.equals(chunk.getHash());
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Loi verify chunk: " + e.getMessage());
            return false;
        }
    }

    public static boolean verifyChunk(byte[] chunkData, String expectedHash) {
        if (chunkData == null || expectedHash == null) {
            return false;
        }

        try {
            String calculatedHash = calculateChunkHash(chunkData);
            return calculatedHash.equals(expectedHash);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Loi verify chunk: " + e.getMessage());
            return false;
        }
    }
}
