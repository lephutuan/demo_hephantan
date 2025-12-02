package Main;

import gui.GUI;
import controllers.FileUploadController;
import services.FileUploadService;
import services.SpeedComparisonService;

public class Main {

    public static void main(String[] args) {
        FileUploadService fileUploadService = new FileUploadService();
        SpeedComparisonService speedService = new SpeedComparisonService(fileUploadService);
        FileUploadController controller = new FileUploadController(fileUploadService, speedService);

        javax.swing.SwingUtilities.invokeLater(() -> new GUI(controller));
    }
}
