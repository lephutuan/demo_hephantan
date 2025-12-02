package p2p;

import models.TorrentFile;
import models.Chunk;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.*;

public class P2PPeer {

    private final int port;
    private ServerSocket serverSocket;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final Map<String, TorrentFile> seedingTorrents = new ConcurrentHashMap<>();
    private volatile boolean running = false;

    public P2PPeer(int port) {
        this.port = port;
    }

    public void startSeeding(TorrentFile torrent) {
        seedingTorrents.put(torrent.getHash(), torrent);
        
        if (!running) {
            running = true;
            executorService.submit(() -> {
                try {
                    if (serverSocket == null || serverSocket.isClosed()) {
                        serverSocket = new ServerSocket(port);
                    }
                    System.out.println("P2P Peer listening on port " + port);
                    
                    while (running && !Thread.currentThread().isInterrupted()) {
                        Socket clientSocket = serverSocket.accept();
                        executorService.submit(() -> handleClient(clientSocket));
                    }
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Loi khi seed: " + e.getMessage());
                    }
                }
            });
        }
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
             DataOutputStream dataOut = new DataOutputStream(clientSocket.getOutputStream())) {
            
            String request = reader.readLine();
            if (request == null) {
                return;
            }

            if (request.startsWith("GET_TORRENT:")) {
                handleGetTorrent(request, writer);
            } else if (request.startsWith("GET_CHUNK:")) {
                handleGetChunk(request, writer, dataOut);
            }
        } catch (IOException e) {
            System.err.println("Loi xu ly client: " + e.getMessage());
        }
    }

    private void handleGetTorrent(String request, PrintWriter writer) {
        String hash = request.substring(12);
        TorrentFile torrent = seedingTorrents.get(hash);
        if (torrent != null) {
            writer.println("TORRENT_FOUND:" + torrent.getFileName() + ":" + torrent.getFileSize() + ":" + torrent.getNumChunks());
        } else {
            writer.println("TORRENT_NOT_FOUND");
        }
    }

    private void handleGetChunk(String request, PrintWriter writer, DataOutputStream dataOut) throws IOException {
        String[] parts = request.substring(10).split(":");
        if (parts.length < 2) {
            writer.println("INVALID_REQUEST");
            return;
        }

        String torrentHash = parts[0];
        int chunkId = Integer.parseInt(parts[1]);

        TorrentFile torrent = seedingTorrents.get(torrentHash);
        if (torrent == null) {
            writer.println("TORRENT_NOT_FOUND");
            return;
        }

        if (chunkId < 0 || chunkId >= torrent.getNumChunks()) {
            writer.println("CHUNK_NOT_FOUND");
            return;
        }

        Chunk chunk;
        try {
            chunk = torrent.getChunks().get(chunkId);
        } catch (IndexOutOfBoundsException e) {
            writer.println("CHUNK_NOT_FOUND");
            return;
        }
        
        writer.println("CHUNK_DATA:" + chunk.getData().length + ":" + chunk.getHash());
        writer.flush();
        
        dataOut.write(chunk.getData());
        dataOut.flush();
    }

    public void stopSeeding(String torrentHash) {
        seedingTorrents.remove(torrentHash);
    }

    public void shutdown() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Loi dong server socket: " + e.getMessage());
        }

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public Map<String, TorrentFile> getSeedingTorrents() {
        return new ConcurrentHashMap<>(seedingTorrents);
    }

    public int getPort() {
        return port;
    }

    public boolean isSeeding(String torrentHash) {
        return seedingTorrents.containsKey(torrentHash);
    }
}
