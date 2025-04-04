package com.apl.controller;

import com.apl.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class RootLayoutController {
    private static final Logger logger = LogManager.getLogger(RootLayoutController.class);

    @FXML
    private final MenuBar menuBar = new MenuBar();
    @FXML
    private Menu fileMenu = new Menu();
    @FXML
    private Menu helpMenu = new Menu();
    @FXML
    private MenuItem buildConnectionString = new MenuItem();
    @FXML
    private MenuItem close = new MenuItem();
    @FXML
    private MenuItem about = new MenuItem();

    private MainApp mainApp;

    @FXML
    private void initialize() {}

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleLoadConfiguration() {
        mainApp.loadConfig();
    }

    @FXML
    private void handleSaveConfiguration() {
        mainApp.saveConfig();
    }

    @FXML
    public void handleBuildConnectionString() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/ConnectionConfiguration.fxml"));
            AnchorPane page = loader.load();
            Scene scene = new Scene(page);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Connection Configuration");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(scene);

            ConnectionConfigurationController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();

            if (controller.getOkClicked()) {
                mainApp.updateConnectionDetails(controller.getUserName(), controller.getPassword(), controller.getConnectionString());
            }
        } catch (IOException e) {
            logger.error("IOException opening Connection Build dialog: {}", e.getMessage());
        }
    }

    @FXML
    private void handleClose() {}

    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(MainApp.getAppName());
        alert.setHeaderText("About");
        String aboutBox = MainApp.getAppName() + " " + MainApp.getVersion();
        aboutBox += "\nbased on: A Programming Legacy";
        aboutBox += "\nhttp://remedylegacy.com";
        alert.setContentText(aboutBox);
        alert.showAndWait();
    }
}
