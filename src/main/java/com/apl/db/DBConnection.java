package com.apl.db;

import com.apl.model.ATTRecord;
import com.apl.model.Form;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.apl.db.Queries.*;

public class DBConnection {
    private static final Logger logger = LogManager.getLogger(DBConnection.class);

    private static DBConnection instance;

    public static DBConnection getInstance(String connectionString, String userName, String password) {
        if (instance == null) {
            instance = new DBConnection(connectionString, userName, password);
        } else if (!connectionString.equals(instance.connectionString) || !userName.equals(instance.userName) || !password.equals(instance.pass)) {
            instance.close();
            instance = new DBConnection(connectionString, userName, password);
        }
        return instance;
    }

    private final String connectionString;
    private final String userName;
    private final String pass;
    private Connection connection;

    private DBConnection(String connectionString, String userName, String pass) {
        this.connectionString = connectionString;
        this.userName = userName;
        this.pass = pass;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public String getUserName() {
        return userName;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null) {
            this.connection = DriverManager.getConnection(connectionString, userName, pass);
        }
        return connection;
    }

    public byte[] getAttachmentBytes(int formID, int fieldID, String entryID) throws SQLException {
        String query = getAttachmentBytesQuery(formID, fieldID, entryID);
        logger.debug("Attachment SQL: {}", query);

        try (Statement statement = getConnection().createStatement()) {
            try (ResultSet byteResults = statement.executeQuery(query)) {
                if (byteResults.next()) {
                    return byteResults.getBytes(1);
                }
            }
        }
        return null;
    }

    public ObservableList<ATTRecord> getAttachmentRecords(int formID, int fieldID, Map<String, Form> formMap, String formName, String fieldName, String whereClause) throws SQLException {
        String query = getAttachmentRecordQuery(formMap, formName, fieldName, whereClause);
        logger.debug("Query being executed: {}", query);

        ObservableList<ATTRecord> recordList = FXCollections.observableArrayList();

        try (Statement statement = getConnection().createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    String entryID = resultSet.getString(1);
                    String fileName = resultSet.getString(2);
                    int fileSize = resultSet.getInt(3);
                    if (fileName != null && !fileName.isEmpty() && fileSize > 0) {
                        ATTRecord attRecord = new ATTRecord(entryID, fileName, fileSize, formID, fieldID, formName, fieldName);
                        recordList.add(attRecord);
                    }
                }
            }
        }

        return recordList;
    }

    public ObservableList<ATTRecord> getAttachmentRecords(String query, int formID, int fieldID, String formName, String fieldName) throws SQLException {
        logger.debug("Query being executed: {}", query);

        ObservableList<ATTRecord> recordList = FXCollections.observableArrayList();

        try (Statement statement = getConnection().createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    String entryID = resultSet.getString(1);
                    String fileName = resultSet.getString(2);
                    int fileSize = resultSet.getInt(3);
                    if (fileName != null && !fileName.isEmpty() && fileSize > 0) {
                        ATTRecord attRecord = new ATTRecord(entryID, fileName, fileSize, formID, fieldID, formName, fieldName);
                        recordList.add(attRecord);
                    }
                }
            }
        }

        return recordList;
    }

    public Map<String, Form> getFormData(List<String> formNameList) throws SQLException {
        String query = getFormDataQuery(formNameList);
        logger.debug("Issuing form query: {}", query);

        try (Statement statement = getConnection().createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(query)) {
                Map<String, Form> formMap = new LinkedHashMap<>();
                int formCount = 0;
                for (; resultSet.next(); formCount++) {
                    String formName = resultSet.getString(1);
                    int formID = resultSet.getInt(2);
                    int resolvedFormID = resultSet.getInt(3);
                    Form form = new Form(formName, formID, resolvedFormID);
                    try (Statement st = getConnection().createStatement()) {
                        String q = Queries.getFieldQuery(formName);
                        logger.debug("Issuing field query: {}", q);

                        try (ResultSet fieldResults = st.executeQuery(q)) {
                            if (fieldResults == null) {
                                return null;
                            }
                            int fieldCount = 0;
                            for (; fieldResults.next(); fieldCount++) {
                                int fieldID = fieldResults.getInt(1);
                                String fieldName = fieldResults.getString(2);
                                form.addField(fieldName, fieldID);
                            }
                            logger.debug("Field Query returned {} records", fieldCount);
                        }
                    }
                    formMap.put(formName, form);
                }
                logger.debug("Form Query returned {} records", formCount);
                return formMap;
            }
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.error("error closing connection", e);
        }
    }
}
