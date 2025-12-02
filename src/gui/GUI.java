package gui;

import controllers.FileUploadController;
import models.PeerInfo;
import models.UploadResult;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class GUI extends JFrame {

    private final FileUploadController controller;

    private JButton btnUploadCS, btnUploadP2P, btnCompare;
    private JProgressBar progressCS, progressP2P;
    private JLabel lblResultCS, lblResultP2P, lblComparison;
    private DefaultListModel<String> peerListModel;
    private JSpinner chunkSpinner;

    private File selectedFileCS, selectedFileP2P;

    public GUI(FileUploadController controller) {
        this.controller = controller;
        setTitle("File Upload Speed Comparison");
        setSize(920, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        JPanel panelMain = new JPanel();
        panelMain.setLayout(new BoxLayout(panelMain, BoxLayout.Y_AXIS));
        panelMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("File Upload Speed Comparison");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelMain.add(title);
        panelMain.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel panelUploads = new JPanel(new GridLayout(1, 2, 20, 20));

        JPanel panelCS = new JPanel();
        panelCS.setBorder(BorderFactory.createTitledBorder("Client-Server Upload"));
        panelCS.setLayout(new BoxLayout(panelCS, BoxLayout.Y_AXIS));

        JButton selectFileCS = new JButton("Chon file CS");
        selectFileCS.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            int res = fc.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                selectedFileCS = fc.getSelectedFile();
                btnUploadCS.setEnabled(true);
            }
        });
        panelCS.add(selectFileCS);

        btnUploadCS = new JButton("Upload via Client-Server");
        btnUploadCS.setEnabled(false);
        btnUploadCS.addActionListener(e -> uploadClientServer());
        panelCS.add(btnUploadCS);

        progressCS = new JProgressBar();
        progressCS.setStringPainted(true);
        panelCS.add(progressCS);

        lblResultCS = new JLabel("Ket qua CS: ");
        panelCS.add(lblResultCS);

        panelUploads.add(panelCS);


        JPanel panelP2P = new JPanel();
        panelP2P.setBorder(BorderFactory.createTitledBorder("P2P Upload"));
        panelP2P.setLayout(new BoxLayout(panelP2P, BoxLayout.Y_AXIS));

        JButton selectFileP2P = new JButton("Chon file P2P");
        selectFileP2P.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            int res = fc.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                selectedFileP2P = fc.getSelectedFile();
                btnUploadP2P.setEnabled(true);
            }
        });
        panelP2P.add(selectFileP2P);

        JPanel chunkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        chunkPanel.add(new JLabel("So chunk:"));
        chunkSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 10000, 1));
        chunkPanel.add(chunkSpinner);
        panelP2P.add(chunkPanel);

        btnUploadP2P = new JButton("Upload via P2P");
        btnUploadP2P.setEnabled(false);
        btnUploadP2P.addActionListener(e -> uploadP2P());
        panelP2P.add(btnUploadP2P);

        progressP2P = new JProgressBar();
        progressP2P.setStringPainted(true);
        panelP2P.add(progressP2P);

        lblResultP2P = new JLabel("Ket qua P2P: ");
        panelP2P.add(lblResultP2P);

        panelUploads.add(panelP2P);

        panelMain.add(panelUploads);
        panelMain.add(Box.createRigidArea(new Dimension(0, 20)));

        btnCompare = new JButton("So sanh toc đo");
        btnCompare.addActionListener(e -> compareSpeeds());
        panelMain.add(btnCompare);

        lblComparison = new JLabel("Ket qua so sanh: ");
        lblComparison.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblComparison.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelMain.add(Box.createRigidArea(new Dimension(0, 10)));
        panelMain.add(lblComparison);

        JPanel panelPeers = new JPanel();
        panelPeers.setBorder(BorderFactory.createTitledBorder("Active P2P Peers"));
        panelPeers.setLayout(new BorderLayout());

        peerListModel = new DefaultListModel<>();
        JList<String> peerList = new JList<>(peerListModel);
        JScrollPane scrollPane = new JScrollPane(peerList);
        panelPeers.add(scrollPane, BorderLayout.CENTER);

        panelMain.add(Box.createRigidArea(new Dimension(0, 20)));
        panelMain.add(panelPeers);

        add(panelMain, BorderLayout.CENTER);

        loadPeers();
    }

    private void uploadClientServer() {
        new Thread(() -> {
            try {
                progressCS.setIndeterminate(true);
                UploadResult result = controller.uploadClientServer(selectedFileCS);
                lblResultCS.setText("Ket qua CS: " + result.getMessage() + " | Toc do: " + String.format("%.2f", result.getSpeedMbps()) + " Mbps");
            } catch (Exception e) {
                lblResultCS.setText("Upload CS that bai: " + e.getMessage());
            } finally {
                progressCS.setIndeterminate(false);
            }
        }).start();
    }

    private void uploadP2P() {
        new Thread(() -> {
            try {
                progressP2P.setIndeterminate(true);
                int numChunks = (Integer) chunkSpinner.getValue();
                UploadResult result = controller.uploadP2P(selectedFileP2P, numChunks);
                lblResultP2P.setText("Ket qua P2P: " + result.getMessage() + " | Toc do: " + String.format("%.2f", result.getSpeedMbps()) + " Mbps");
            } catch (Exception e) {
                lblResultP2P.setText("Upload P2P that bai: " + e.getMessage());
            } finally {
                progressP2P.setIndeterminate(false);
            }
        }).start();
    }

    private void compareSpeeds() {
        if (selectedFileCS == null && selectedFileP2P == null) {
            JOptionPane.showMessageDialog(this, "Chua chon file de so sanh.");
            return;
        }
        new Thread(() -> {
            try {
                File fileToCompare = selectedFileCS != null ? selectedFileCS : selectedFileP2P;
                int numChunks = (Integer) chunkSpinner.getValue();
                var result = controller.compareSpeeds(fileToCompare, numChunks);
                lblComparison.setText("Ket qua: " + result.getRecommendation() + " | CS: "
                        + String.format("%.2f", result.getClientServerResult().getSpeedMbps()) + " Mbps, P2P: "
                        + String.format("%.2f", result.getP2pResult().getSpeedMbps()) + " Mbps (chunks: " + numChunks + ")");
            } catch (Exception e) {
                lblComparison.setText("So sanh that bai: " + e.getMessage());
            }
        }).start();
    }

    private void loadPeers() {
        peerListModel.clear();
        List<PeerInfo> peers = controller.getActivePeers();
        for (PeerInfo p : peers) {
            peerListModel.addElement(p.getPeerId() + " - " + p.getIpAddress() + ":" + p.getPort());
        }
    }
}
