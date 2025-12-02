package p2p;

import models.TorrentFile;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.*;

public class P2PPeer {

    private final int port;
    private ServerSocket serverSocket;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final Map<String, TorrentFile> seedingTorrents = new ConcurrentHashMap<>();

    public P2PPeer(int port) {
        this.port = port;
    }

    public void startSeeding(TorrentFile torrent) {
        seedingTorrents.put(torrent.getHash(), torrent);
        executorService.submit(() -> {
            try {
                if (serverSocket == null) {
                    serverSocket = new ServerSocket(port);
                }
                while (!Thread.currentThread().isInterrupted()) {
                    Socket clientSocket = serverSocket.accept();
                    executorService.submit(() -> handleClient(clientSocket));
                }
            } catch (IOException e) {
                System.err.println("Loi khi seed: " + e.getMessage());
            }
        });
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String request = reader.readLine();
            if (request != null && request.startsWith("GET_TORRENT:")) {
                String hash = request.substring(12);
                TorrentFile torrent = seedingTorrents.get(hash);
                if (torrent != null) {
                    writer.println("TORRENT_FOUND:" + torrent.getFileName() + ":" + torrent.getFileSize() + ":" + torrent.getNumChunks());
                } else {
                    writer.println("TORRENT_NOT_FOUND");
                }
            }
        } catch (IOException e) {
            System.err.println("Loi xu ly client: " + e.getMessage());
        }
    }
}
