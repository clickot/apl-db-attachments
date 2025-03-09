package com.apl;

import com.apl.model.ATTRecord;
import com.apl.model.DBConnection;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Exporter {

    private Exporter() {}

    public static String export(ObservableList<ATTRecord> selectedItems, DBConnection dbConn, boolean bulk, File selectedDirectory, boolean formFolder, boolean entryIDFolder, boolean fieldNameFolder) {
        FileChooser fileChooser = new FileChooser();
        StringBuilder output = new StringBuilder();
        File file;

        for (ATTRecord record : selectedItems) {
            String fileName = record.getFileName().getValue();
            byte[] fileBytes = record.getBytes(dbConn);
            if (fileBytes == null) {
                output.append("Unable to get the file ").append(record.getEntryID().getValue()).append(":").append(record.getFieldName()).append(":").append(fileName).append(" from the DB server, it returned 0 bytes").append(System.lineSeparator());
            } else {
                if (bulk) {
                    String saveFileFolder = "";
                    String saveFileName = "";
                    if (formFolder) {
                        saveFileFolder = stripNonOSValues(record.getFormName());
                    } else {
                        saveFileName = stripNonOSValues(record.getFormName());
                    }

                    if (entryIDFolder) {
                        saveFileFolder = saveFileFolder + File.separator + record.getEntryID().getValue();
                    } else {
                        saveFileName = saveFileName + "-" + record.getEntryID().getValue();
                    }

                    if (fieldNameFolder) {
                        saveFileFolder = saveFileFolder + File.separator + record.getFieldName();
                    } else {
                        saveFileName = saveFileName + "-" + stripNonOSValues(record.getFieldName());
                    }

                    saveFileName = saveFileName + "-" + record.getFileName().getValue();
                    if (saveFileName.startsWith("-")) {
                        saveFileName = saveFileName.substring(1);
                    }

                    File f = new File(selectedDirectory.getAbsolutePath() + File.separator + saveFileFolder);
                    if (!f.exists()) {
                        f.mkdirs();
                    }

                    file = new File(selectedDirectory.getAbsolutePath() + File.separator + saveFileFolder + File.separator + saveFileName);
                } else {
                    fileChooser.setInitialFileName(fileName);
                    fileChooser.setTitle("Save Attachment");
                    file = fileChooser.showSaveDialog(null);
                }

                if (file != null) {
                    try {
                        FileOutputStream fos = new FileOutputStream(file.toString());
                        fos.write(fileBytes);
                        fos.close();
                    } catch (IOException e) {
                        output.append("Error saving file: ").append(record.getEntryID().getValue()).append(":").append(record.getFieldName()).append(":").append(fileName).append(": ").append(e.getMessage());
                    }
                }
            }
        }

        return output.toString();
    }

    private static String stripNonOSValues(String s) {
        return s.replaceAll("[~#%&*{}:<>?/+|\"]", "_");
    }
}
