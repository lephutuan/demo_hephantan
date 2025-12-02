package p2p;

import models.Chunk;
import models.PeerInfo;
import services.ChunkVerifier;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class ChunkDownloader {

    private final ExecutorService executorService;
    private final PeerSelector peerSelector;
    private final int maxConcurrentDownloads;

    public ChunkDownloader(int maxConcurrentDownloads) {
        this.maxConcurrentDownloads = maxConcurrentDownloads;
        this.executorService = Executors.newFixedThreadPool(maxConcurrentDownloads);
        this.peerSelector = new PeerSelector();
    }

    public List<Chunk> downloadChunksParallel(List<PeerInfo> availablePeers, List<Integer> chunkIds, String torrentHash) 
            throws InterruptedException, ExecutionException {
        
        if (chunkIds == null || chunkIds.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Integer, PeerInfo> chunkToPeerMap = peerSelector.distributeChunksToPeers(availablePeers, chunkIds);
        List<Future<Chunk>> futures = new ArrayList<>();

        for (Integer chunkId : chunkIds) {
            PeerInfo peer = chunkToPeerMap.get(chunkId);
            if (peer != null) {
                Future<Chunk> future = executorService.submit(() -> downloadChunk(peer, chunkId, torrentHash));
                futures.add(future);
            } else {
                System.err.println("Khong tim thay peer cho chunk: " + chunkId);
            }
        }

        List<Chunk> downloadedChunks = new ArrayList<>();
        for (Future<Chunk> future : futures) {
            try {
                Chunk chunk = future.get();
                if (chunk != null) {
                    downloadedChunks.add(chunk);
                }
            } catch (ExecutionException e) {
                System.err.println("Loi download chunk: " + e.getCause().getMessage());
            }
        }

        downloadedChunks.sort(Comparator.comparingInt(Chunk::getId));
        return downloadedChunks;
    }

    private Chunk downloadChunk(PeerInfo peer, int chunkId, String torrentHash) {
        peer.incrementLoad();
        
        try (Socket socket = new Socket(peer.getIpAddress(), peer.getPort());
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             DataInputStream dataIn = new DataInputStream(socket.getInputStream())) {

            out.println("GET_CHUNK:" + torrentHash + ":" + chunkId);

            String response = in.readLine();
            if (response != null && response.startsWith("CHUNK_DATA:")) {
                String[] parts = response.split(":");
                int size = Integer.parseInt(parts[1]);
                String hash = parts[2];

                byte[] chunkData = new byte[size];
                dataIn.readFully(chunkData);

                if (ChunkVerifier.verifyChunk(chunkData, hash)) {
                    Chunk chunk = new Chunk(chunkId, chunkData, hash);
                    chunk.setDownloaded(true);
                    System.out.println("Downloaded and verified chunk " + chunkId + " from peer " + peer.getPeerId());
                    return chunk;
                } else {
                    System.err.println("Chunk verification failed for chunk " + chunkId);
                    return null;
                }
            } else {
                System.err.println("Khong nhan duoc chunk data tu peer " + peer.getPeerId());
                return null;
            }

        } catch (IOException e) {
            System.err.println("Loi download chunk " + chunkId + " tu peer " + peer.getPeerId() + ": " + e.getMessage());
            return null;
        } finally {
            peer.decrementLoad();
        }
    }

    public File mergeChunks(List<Chunk> chunks, String outputPath) throws IOException {
        File outputFile = new File(outputPath);
        outputFile.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            for (Chunk chunk : chunks) {
                fos.write(chunk.getData());
            }
        }

        System.out.println("Merged " + chunks.size() + " chunks into: " + outputPath);
        return outputFile;
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
