package com.apl.model;

import com.apl.db.DBConnection;
import com.bmc.arsys.arcompress.ARDecompressor;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.SQLException;
import java.util.zip.DataFormatException;

public class ATTRecord {
    private static final Logger logger = LogManager.getLogger(ATTRecord.class);

    private final StringProperty entryID;
    private final StringProperty fileName;
    private final int formID;
    private final int fieldID;
    private final String formName;
    private final String fieldName;
    private final ObservableValue<Integer> originalSize;

    public ATTRecord(String entryID, String fileName, int originalSize, int formID, int fieldID, String formName, String fieldName) {
        this.entryID = new SimpleStringProperty(entryID);
        this.originalSize = new ReadOnlyObjectWrapper<>(originalSize);
        this.formID = formID;
        this.fieldID = fieldID;
        this.formName = formName;
        this.fieldName = fieldName;
        if (fileName.contains(File.separator)) {
            fileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
        }

        this.fileName = new SimpleStringProperty(fileName);
    }

    public byte[] getBytes(DBConnection dbConnection) throws SQLException {
        byte[] bytes = dbConnection.getAttachmentBytes(formID, fieldID, entryID.getValue());
        if (bytes != null) {
            ARDecompressor arDecompressor = new ARDecompressor();
            arDecompressor.setInput(bytes);
            try {
                bytes = arDecompressor.inflate();
            } catch (DataFormatException e) {
                logger.error("Error Decompressing: {}", e.getMessage());
                bytes = new byte[0];
            }
        } else {
            logger.error("Bytes null");
            bytes = new byte[0];
        }
        return bytes;
    }

    public StringProperty getEntryID() {
        return this.entryID;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public StringProperty getFileName() {
        return this.fileName;
    }

    public String getFormName() {
        return this.formName;
    }

    public ObservableValue<Integer> getOriginalSize() {
        return this.originalSize;
    }
}
