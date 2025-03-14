package com.apl.controller;

import com.apl.MainApp;
import com.apl.db.DBConnection;
import com.apl.db.Exporter;
import com.apl.model.ATTRecord;
import com.apl.model.Field;
import com.apl.model.Form;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static com.apl.db.Queries.getAttachmentRecordQuery;

public class MainOverviewController {
    private static final Logger logger = LogManager.getLogger(MainOverviewController.class);

    @FXML
    private TextField connectionString = new TextField();
    @FXML
    private TableColumn<ATTRecord, String> entryIDColumn = new TableColumn<>();
    @FXML
    private TableView<ATTRecord> entryTable = new TableView<>();
    @FXML
    private ComboBox<String> fieldComboBox = new ComboBox<>();
    @FXML
    private TableColumn<ATTRecord, String> fileNameColumn = new TableColumn<>();
    @FXML
    private TableColumn<ATTRecord, Integer> fileSizeColumn = new TableColumn<>();
    @FXML
    private ComboBox<String> formComboBox = new ComboBox<>();
    @FXML
    private TextField password = new TextField();
    @FXML
    private Label recordCountLabel = new Label();
    @FXML
    private TextField recordsQuery = new TextField();
    @FXML
    private Label statusBar = new Label();
    @FXML
    private TextField userName = new TextField();

    private Map<String, Form> formMap;
    private boolean fieldNameFolder = false;
    private boolean formFolder = true;
    private boolean entryIDFolder = false;
    private MainApp mainApp;
    private DBConnection dbConnection;

    public String getConnectionString() {
        return connectionString.getText();
    }

    public void setConnectionString(TextField connectionString) {
        this.connectionString.setText(connectionString.getText());
    }

    public String getPassword() {
        return password.getText();
    }

    public void setPassword(TextField password) {
        this.password.setText(password.getText());
    }

    public String getUserName() {
        return userName.getText();
    }

    public void setUserName(TextField userName) {
        this.userName.setText(userName.getText());
    }

    public DBConnection getDBConnection() {
        if (dbConnection == null) {
            dbConnection = DBConnection.getInstance(getConnectionString(), getUserName(), getPassword());
        }
        return dbConnection;
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleBuildConnectionString() {
        this.mainApp.handleBuildConnectionString();
    }

    @FXML
    private void handleClearSelection() {
        this.entryTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleConnect() {
        try {
            statusBar.setText("Starting Connection Process");
            getDBConnection().getConnection();
            openAlert(AlertType.INFORMATION, "Success", "Connection Succeeded");
            statusBar.setText("Loading Forms List");
            formMap = dbConnection.getFormData(null);
            statusBar.setText("Forms List Loaded");
            formComboBox.getItems().clear();
            fieldComboBox.getItems().clear();
            recordsQuery.clear();
            entryTable.getItems().clear();
            formComboBox.setTooltip(new Tooltip());
            if (formMap != null) {
                for (String formName : formMap.keySet()) {
                    formComboBox.getItems().add(formName);
                }
                new ComboBoxAutoComplete<>(formComboBox);
                statusBar.setText("Ready To Start");
            } else {
                statusBar.setText("Error loading FormMap");
            }
        } catch (SQLException e) {
            logger.error("error in db operation: {}", e.getMessage());
            openAlert(AlertType.ERROR, "Error", "DB Operation Failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleExecute() {
        entryTable.getItems().clear();
        Form form = formMap.get(formComboBox.getValue());

        ObservableList<ATTRecord> recordList = null;
        try {
            recordList = getDBConnection().getAttachmentRecords(recordsQuery.getText(), form.getResolvedID(), form.getField(fieldComboBox.getValue()).getId(), formComboBox.getValue(), fieldComboBox.getValue());
        } catch (SQLException e) {
            logger.error("error getting attachment records", e);
        }

        if (recordList != null) {
            entryTable.setItems(recordList);
            if (recordList.size() == 1) {
                recordCountLabel.setText(recordList.size() + " record");
            } else if (recordList.size() > 1) {
                recordCountLabel.setText(recordList.size() + " records");
            } else {
                recordCountLabel.setText("0 records");
            }
        }
    }

    @FXML
    private void handleFieldSelection() {
        recordsQuery.setText(getAttachmentRecordQuery(formMap, formComboBox.getValue(), fieldComboBox.getValue(), null));
    }

    @FXML
    private void handleFormSelection() {
        Form form = formMap.get(formComboBox.getValue());
        List<Field> fieldList = null;
        if (form != null) {
            fieldList = form.getFieldList();
        }

        fieldComboBox.getItems().clear();
        recordsQuery.clear();
        entryTable.getItems().clear();
        if (fieldList != null) {
            for (Field field : fieldList) {
                fieldComboBox.getItems().add(field.getName());
            }
        }
    }

    @FXML
    private void handleSaveBulk() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/BulkExport.fxml"));
            AnchorPane page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Bulk Export");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            BulkExportController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();
            if (controller.getOkClicked()) {
                formFolder = controller.isFormFolder();
                entryIDFolder = controller.isEntryIDFolder();
                fieldNameFolder = controller.isFieldNameFolder();
                saveFiles(true);
            }

        } catch (IOException | SQLException e) {
            logger.error("IOException opening Bulk Export dialog: {}", e.getMessage());
        }
    }

    @FXML
    private void handleSavePrompt() {
        try {
            saveFiles(false);
        } catch (SQLException | IOException e) {
            logger.error("error saving files", e);
        }
    }

    @FXML
    private void handleSelectAll() {
        entryTable.getSelectionModel().selectAll();
    }

    @FXML
    private void initialize() {
        entryIDColumn.setCellValueFactory((cellData) -> cellData.getValue().getEntryID());
        fileNameColumn.setCellValueFactory((cellData) -> cellData.getValue().getFileName());
        fileSizeColumn.setCellValueFactory((cellData) -> cellData.getValue().getOriginalSize());
        entryTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void openAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.initOwner(mainApp.getPrimaryStage());
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void saveFiles(boolean bulk) throws SQLException, IOException {
        statusBar.setText("Export Process Starting");
        ObservableList<ATTRecord> selectedItems = entryTable.getSelectionModel().getSelectedItems();
        File selectedDirectory = null;
        if (bulk) {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            selectedDirectory = directoryChooser.showDialog(null);
            if (selectedDirectory == null) {
                return;
            }
        }

        String output = new Exporter(getConnectionString(), getUserName(), getPassword()).export(selectedItems, bulk, selectedDirectory, formFolder, entryIDFolder, fieldNameFolder);
        if (!output.isEmpty()) {
            openAlert(AlertType.ERROR, "Error", output);
        }

        statusBar.setText("Export completed");
    }
}
