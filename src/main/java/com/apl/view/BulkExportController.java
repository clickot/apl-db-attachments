package com.apl.view;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;

public class BulkExportController {
    @FXML
    private RadioButton formFolderRadio = new RadioButton();
    @FXML
    private RadioButton formFileRadio = new RadioButton();
    @FXML
    private RadioButton entryIDFolderRadio = new RadioButton();
    @FXML
    private RadioButton entryIDFileRadio = new RadioButton();
    @FXML
    private RadioButton fieldNameFolderRadio = new RadioButton();
    @FXML
    private RadioButton fieldNameFileRadio = new RadioButton();

    private Stage dialogStage;
    private boolean okClicked = false;
    private boolean formFolder = true;
    private boolean entryIDFolder = false;
    private boolean fieldNameFolder = false;

    @FXML
    private void initialize() {}

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void handleFormSelection() {
        if (formFolderRadio.isSelected()) {
            formFolder = true;
        } else {
            formFolder = false;
            entryIDFolder = false;
            fieldNameFolder = false;
            entryIDFileRadio.setSelected(true);
            fieldNameFileRadio.setSelected(true);
        }

    }

    @FXML
    private void handleEntryIDSelection() {
        if (entryIDFolderRadio.isSelected()) {
            formFolder = true;
            entryIDFolder = true;
            formFolderRadio.setSelected(true);
        } else {
            entryIDFolder = false;
            fieldNameFolder = false;
            fieldNameFileRadio.setSelected(true);
        }
    }

    @FXML
    private void handleFieldNameSelection() {
        if (fieldNameFolderRadio.isSelected()) {
            formFolder = true;
            entryIDFolder = true;
            fieldNameFolder = true;
            formFolderRadio.setSelected(true);
            entryIDFolderRadio.setSelected(true);
        } else {
            fieldNameFolder = false;
        }

    }

    @FXML
    private void handleOk() {
        okClicked = true;
        dialogStage.close();
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    public boolean getOkClicked() {
        return okClicked;
    }

    public boolean isFormFolder() {
        return formFolder;
    }

    public boolean isEntryIDFolder() {
        return entryIDFolder;
    }

    public boolean isFieldNameFolder() {
        return fieldNameFolder;
    }
}
