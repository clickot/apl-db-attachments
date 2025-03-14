package com.apl.db;

import com.apl.model.ATTRecord;
import com.apl.model.Field;
import com.apl.model.Form;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Exporter {
    private static final Logger logger = LogManager.getLogger(Exporter.class);

    private final DBConnection dbConnection;

    public Exporter(String connectionString, String userName, String password) {
        this.dbConnection = DBConnection.getInstance(connectionString, userName, password);
    }

    public void export(String outputDir, String formArray, String fieldArray, String whereClause, boolean formFolder, boolean entryIDFolder, boolean fieldNameFolder) throws SQLException, IOException {
        List<String> formNameList = Arrays.asList(formArray != null ? formArray.split("\\s*,\\s*") : new String[0]);
        List<String> fieldNameList = Arrays.asList(fieldArray != null ? fieldArray.split("\\s*,\\s*") : new String[0]);

        logger.info("Starting Export with following configuration:\n{}", exportInfo(formNameList, fieldNameList, outputDir, whereClause, formFolder, entryIDFolder, fieldNameFolder));

        Map<String, Form> formMap = dbConnection.getFormData(formNameList);
        logger.info("Total of {} forms retrieved", formMap.size());
        if (logger.isDebugEnabled()) {
            logger.debug("Complete list of forms that contain attachment fields\n{}", String.join("\n", formMap.keySet()));
        }

        for (String formName : formMap.keySet()) {
            if (formNameList.contains(formName) || formNameList.isEmpty()) {
                logger.info("Starting processing of form '{}'", formName);
                Form form = formMap.get(formName);

                for (Field field : form.getFieldList()) {
                    String fieldName = field.getName();
                    if (fieldNameList.contains(fieldName) || fieldNameList.isEmpty()) {
                        logger.info("Starting processing of field '{}'", fieldName);

                        ObservableList<ATTRecord> attachmentRecordList = dbConnection.getAttachmentRecords(form.getResolvedID(), field.getId(), formMap, formName, fieldName, whereClause);

                        if (attachmentRecordList != null) {
                            logger.info("{} record(s) found to export", attachmentRecordList.size());
                            if (!attachmentRecordList.isEmpty()) {
                                String output = export(attachmentRecordList, true, new File(outputDir), formFolder, entryIDFolder, fieldNameFolder);
                                if (!output.isEmpty()) {
                                    logger.error(output);
                                }

                                logger.info("Export of records complete");
                            }
                        } else {
                            logger.error("Error occurred getting records to export");
                        }
                    }
                }
            }
        }
        logger.info("Export Complete");
    }

    public String export(ObservableList<ATTRecord> selectedItems, boolean bulk, File selectedDirectory, boolean formFolder, boolean entryIDFolder, boolean fieldNameFolder) throws SQLException, IOException {
        FileChooser fileChooser = new FileChooser();
        StringBuilder output = new StringBuilder();

        for (ATTRecord record : selectedItems) {
            String fileName = record.getFileName().getValue();
            byte[] fileBytes = record.getBytes(this.dbConnection);
            if (fileBytes == null) {
                output.append("Unable to get the file ").append(record.getEntryID().getValue()).append(":").append(record.getFieldName()).append(":").append(fileName).append(" from the DB server, it returned 0 bytes").append(System.lineSeparator());
            } else {
                File file;
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
                    File dir = Path.of(selectedDirectory.getPath(),saveFileFolder).toAbsolutePath().toFile();
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    file = Path.of(dir.getPath(), saveFileName).toFile();
                } else {
                    fileChooser.setInitialFileName(fileName);
                    fileChooser.setTitle("Save Attachment");
                    file = fileChooser.showSaveDialog(null);
                }
                if (file != null) {
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(fileBytes);
                    }
                }
            }
        }
        return output.toString();
    }

    private String exportInfo(List<String> formNameList, List<String> fieldNameList, String outputDir, String whereClause, boolean formFolder, boolean entryIDFolder, boolean fieldNameFolder) {
        String exportInfo = "Connecting to db using " + dbConnection.getUserName() + " and connection string '" + dbConnection.getConnectionString() + "'";
        exportInfo += "\nExporting to directory '" + outputDir + "'";
        if (!formNameList.isEmpty()) {
            exportInfo += "\nFrom the following forms:" + String.join("\nt", formNameList);
        } else {
            exportInfo += "\nFrom all forms containing attachment data";
        }
        if (!fieldNameList.isEmpty()) {
            exportInfo += "\nFrom the following fields:" + String.join("\nt", fieldNameList);
        } else {
            exportInfo += "\nFrom all attachment fields";
        }
        if (whereClause != null && !whereClause.isEmpty()) {
            exportInfo += "\nWith the following where clause: " + whereClause;
        }
        exportInfo += "\nWith the following file name conventions:";
        exportInfo += "\nForm Name as Folder: " + formFolder;
        exportInfo += "\nEntry ID as Folder: " + entryIDFolder;
        exportInfo += "\nField Name as Folder: " + fieldNameFolder;

        return exportInfo;
    }

    private static String stripNonOSValues(String s) {
        return s.replaceAll("[~#%&*{}:<>?/+|\"]", "_");
    }
}
