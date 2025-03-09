package com.apl.db;

import com.apl.model.ATTRecord;
import com.apl.model.Field;
import com.apl.model.Form;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Exporter {
    private static final Logger logger = LogManager.getLogger(Exporter.class);

    private final DBConnection dbConnection;

    public Exporter(String connectionString, String userName, String password) {
        this.dbConnection = DBConnection.getInstance(connectionString, userName, password);
    }

    private static String stripNonOSValues(String s) {
        return s.replaceAll("[~#%&*{}:<>?/+|\"]", "_");
    }

    public void export(String formArray, String fieldArray, String outputDir, String whereClause, boolean formFolder, boolean entryIDFolder, boolean fieldNameFolder) throws SQLException, IOException {
        List<String> formNameList = new ArrayList<>();
        List<String> fieldNameList = new ArrayList<>();
        if (formArray != null && !formArray.isEmpty()) {
            String[] tokens = formArray.split(",");

            for (String token : tokens) {
                formNameList.add(token.trim());
            }
        }

        if (fieldArray != null && !fieldArray.isEmpty()) {
            String[] tokens = fieldArray.split(",");

            for (String token : tokens) {
                if (token != null && !token.isEmpty()) {
                    fieldNameList.add(token.trim());
                }
            }
        }

        logger.info("Starting Export with following configuration:");
        logger.info("Connecting to db using {}  and connection string '{}'", dbConnection.getUserName(), dbConnection.getConnectionString());
        logger.info("Exporting to directory '{}'", outputDir);
        if (!formNameList.isEmpty()) {
            logger.info("From the following forms:");

            for (String formName : formNameList) {
                logger.info("\t{}", formName);
            }
        } else {
            logger.info("From all forms containing attachment data");
        }

        if (!fieldNameList.isEmpty()) {
            logger.info("From the following fields:");

            for (String fieldName : fieldNameList) {
                logger.info("\t{}", fieldName);
            }
        } else {
            logger.info("From all attachment fields");
        }

        if (whereClause != null && !whereClause.isEmpty()) {
            logger.info("With the following where clause: {}", whereClause);
        }

        logger.info("With the following file name conventions:");
        logger.info("Form Name as Folder: {}", formFolder);
        logger.info("Entry ID as Folder: {}", entryIDFolder);
        logger.info("Field Name as Folder: {}", fieldNameFolder);

        logger.info("Getting forms from server");
        Map<String, Form> formMap = dbConnection.getFormData(formNameList);
        logger.info("Total of {} form retrieved", formMap.size());
        if (logger.isDebugEnabled()) {
            logger.debug("Complete list of forms that contain attachment fields");

            for (String formName : formMap.keySet()) {
                logger.debug(formName);
            }
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
        File file;

        for (ATTRecord record : selectedItems) {
            String fileName = record.getFileName().getValue();
            byte[] fileBytes = record.getBytes(this.dbConnection);
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
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(fileBytes);
                    }
                }
            }
        }
        return output.toString();
    }
}
