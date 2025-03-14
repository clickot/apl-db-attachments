package com.apl.view;

import com.apl.db.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.sql.SQLException;

public class ConnectionConfigurationController {
    @FXML
    private TextField connectionString = new TextField();
    @FXML
    private TextField databaseName = new TextField();
    @FXML
    private Label databaseNameLabel = new Label();
    @FXML
    private ComboBox<String> dbTypeComboBox = new ComboBox<>();
    @FXML
    private RadioButton oracleService = new RadioButton();
    @FXML
    private RadioButton oracleSID = new RadioButton();
    @FXML
    private ToggleGroup oracleTypeGroup = new ToggleGroup();
    @FXML
    private TextField password = new TextField();
    @FXML
    private TextField serverDetails = new TextField();
    @FXML
    private Label serverDetailsLabel = new Label();
    @FXML
    private TextField serverName = new TextField();
    @FXML
    private TextField serverPort = new TextField();
    @FXML
    private TextField userName = new TextField();

    private Stage dialogStage;
    private boolean okClicked = false;

    private void buildConnectionString() {
        String s = "";
        if (!dbTypeComboBox.getSelectionModel().isEmpty()) {
            if (dbTypeComboBox.getSelectionModel().getSelectedItem().equals("SQL Server")) {
                s = "jdbc:sqlserver://";
                if (!serverName.getText().trim().isEmpty()) {
                    s += serverName.getText();
                }

                if (!serverPort.getText().trim().isEmpty()) {
                    s += ":" + serverPort.getText();
                }

                if (!databaseName.getText().trim().isEmpty()) {
                    s += ";databaseName=" + databaseName.getText();
                }

                if (!serverDetails.getText().trim().isEmpty()) {
                    s += ";instance=" + serverDetails.getText();
                }
            } else if (dbTypeComboBox.getSelectionModel().getSelectedItem().equals("Oracle")) {
                s = "jdbc:oracle:thin:";
                if (!serverName.getText().trim().isEmpty()) {
                    if (oracleSID.isSelected()) {
                        s += "@" + serverName.getText();
                    } else if (oracleService.isSelected()) {
                        s += "@//" + serverName.getText();
                    }
                }

                if (!serverPort.getText().trim().isEmpty()) {
                    s += ":" + serverPort.getText();
                }

                if (!serverDetails.getText().trim().isEmpty()) {
                    if (oracleSID.isSelected()) {
                        s += ":" + serverDetails.getText();
                    } else if (oracleService.isSelected()) {
                        s += "/" + serverDetails.getText();
                    }
                }
            } else if (dbTypeComboBox.getSelectionModel().getSelectedItem().equals("PostgreSQL")) {
                s = "jdbc:postgresql://";
                if (!serverName.getText().trim().isEmpty()) {
                    s += serverName.getText();
                }

                if (!serverPort.getText().trim().isEmpty()) {
                    s += ":" + serverPort.getText();
                }

                if (!databaseName.getText().trim().isEmpty()) {
                    s += "/" + databaseName.getText();
                }
            }
        }

        connectionString.setText(s);
    }

    public TextField getConnectionString() {
        return connectionString;
    }

    public boolean getOkClicked() {
        return okClicked;
    }

    public TextField getPassword() {
        return password;
    }

    public TextField getUserName() {
        return userName;
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    @FXML
    private void handleDataChange() {
        buildConnectionString();
    }

    @FXML
    private void handleDBType() {
        serverDetailsLabel.setVisible(true);
        if ((dbTypeComboBox.getSelectionModel().getSelectedItem()).equals("SQL Server")) {
            sqlserverSelected();
        } else if ((dbTypeComboBox.getSelectionModel().getSelectedItem()).equals("Oracle")) {
            oracleSelected();
        } else if ((dbTypeComboBox.getSelectionModel().getSelectedItem()).equals("PostgreSQL")) {
            postgresSelected();
        }
        buildConnectionString();
    }

    private void oracleSelected() {
        databaseNameLabel.setVisible(false);
        databaseName.setVisible(false);
        handleOracleTypeSelect();
        oracleSID.setVisible(true);
        oracleService.setVisible(true);
        serverDetails.setVisible(true);
        serverDetailsLabel.setVisible(true);
        if (serverPort.getText().isEmpty() || serverPort.getText().equals("1433") || serverPort.getText().equals("5432")) {
            serverPort.setText("1521");
        }
    }

    private void postgresSelected() {
        databaseNameLabel.setVisible(true);
        databaseName.setVisible(true);
        serverDetails.setVisible(false);
        serverDetailsLabel.setVisible(false);
        oracleSID.setVisible(false);
        oracleService.setVisible(false);
        if (serverPort.getText().isEmpty() || serverPort.getText().equals("1433") || serverPort.getText().equals("1521")) {
            serverPort.setText("5432");
        }
    }

    private void sqlserverSelected() {
        databaseNameLabel.setVisible(true);
        databaseName.setVisible(true);
        serverDetailsLabel.setText("Server Instance");
        serverDetailsLabel.setVisible(true);
        serverDetails.setVisible(true);
        oracleSID.setVisible(false);
        oracleService.setVisible(false);
        if (serverPort.getText().isEmpty() || serverPort.getText().equals("1521") || serverPort.getText().equals("5432")) {
            serverPort.setText("1433");
        }
    }

    @FXML
    private void handleOk() {
        okClicked = true;
        dialogStage.close();
    }

    @FXML
    private void handleOracleTypeSelect() {
        if (oracleSID.isSelected()) {
            serverDetailsLabel.setText("SID");
        }

        if (oracleService.isSelected()) {
            serverDetailsLabel.setText("Service");
        }

        buildConnectionString();
    }

    @FXML
    private void handleTestConnection() {
        try {
            DBConnection.getInstance(connectionString.getText(), userName.getText(), password.getText()).getConnection();
            openAlert(AlertType.INFORMATION, "Success", "Connection Succeeded");
        } catch (SQLException e) {
            openAlert(AlertType.ERROR, "Error", "Connection Failed: " + e.getMessage());
        }

    }

    @FXML
    private void initialize() {
        dbTypeComboBox.getItems().add("Oracle");
        dbTypeComboBox.getItems().add("SQL Server");
        dbTypeComboBox.getItems().add("PostgreSQL");
        // preselect oracle server per default
        dbTypeComboBox.getSelectionModel().selectFirst();
        oracleSelected();
    }

    private void openAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
        this.dialogStage.addEventHandler(KeyEvent.KEY_RELEASED, (event) -> {
            if (KeyCode.ESCAPE == event.getCode()) {
                handleCancel();
            }
        });
    }
}
