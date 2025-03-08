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
        if (connection == null) {this.connection = DriverManager.getConnection(connectionString, userName, pass);}
        return connection;
    }

    public ResultSet getAttachmentBytes(int formID, int fieldID, String entryID) throws SQLException {
        String query = "select c" + fieldID + " from b" + formID + "c" + fieldID + " where entryID = '" + entryID + "'";
        logger.debug("Attachment SQL: {}", query);

        try (Statement statement = getConnection().createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(query)) {
                return resultSet;
            }
        }
    }

    public String getAttachmentRecordQuery(Map<String, Form> formMap, String formName, String fieldName, String whereClause) {
        String query = "";
        int formID;
        int fieldID;
        Form form;
        if (formName != null && !formName.isEmpty()) {
            form = formMap.get(formName);
            if (form.getID() == form.getResolvedID()) {
                formID = form.getID();
            } else {
                formID = form.getResolvedID();
            }

            if (fieldName != null && !fieldName.isEmpty()) {
                fieldID = form.getField(fieldName).getId();
                if (formID > 0 && fieldID > 0) {
                    query = "select b.c1 AttReqID, b.c" + fieldID + " AttFileName, b.co" + fieldID + " AttSize from b" + formID + " b inner join t" + formID + " t on t.c1 = b.c1";
                    if (whereClause != null && !whereClause.trim().isEmpty()) {
                        query = query + " where " + whereClause;
                    }
                }

                return query;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public ObservableList<ATTRecord> getAttachmentRecords(String query, int formID, int resolvedFormID, int fieldID, String formName, String fieldName) throws SQLException {
        ObservableList<ATTRecord> recordList = FXCollections.observableArrayList();

        try (Statement statement = getConnection().createStatement()) {
            logger.debug("Query being executed: {}", query);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    String entryID = resultSet.getString(1);
                    String fileName = resultSet.getString(2);
                    int fileSize = resultSet.getInt(3);
                    if (fileName != null && !fileName.isEmpty() && fileSize > 0) {
                        ATTRecord attRecord = new ATTRecord(entryID, fileName, fileSize, formID, resolvedFormID, fieldID, formName, fieldName);
                        recordList.add(attRecord);
                    }
                }
            }
        }

        return recordList;
    }

    public Map<String, Form> getFormData(List<String> formNameList) throws SQLException {
        String query = "select distinct a.name, a.schemaid, a.resolvedschemaid from arschema a inner join field f on a.schemaid = f.schemaId inner join field_attach fa on a.schemaId = fa.schemaId and f.fieldId = fa.fieldId where a.schemaType = 1 and f.fOption < 4 ";
        if (formNameList != null && !formNameList.isEmpty()) {
            StringBuilder s = new StringBuilder();
            for (String formName : formNameList) {s.append(",'").append(formName).append("'");}
            query = query + "and a.name in (" + s.substring(1) + ")";
        }

        query = query + " order by a.name";
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
                        String q = "select distinct f.fieldID, f.fieldName from arschema a inner join field f on a.schemaid = f.schemaId inner join field_attach fa on a.schemaId = fa.schemaId and f.fieldId = fa.fieldId where a.name = '" + formName + "' and f.fOption < 4" + " order by f.fieldName";
                        logger.debug("Issuing field query: {}", q);
                        ResultSet fieldResults = st.executeQuery(q);
                        if (fieldResults == null) {return null;}

                        int fieldCount = 0;
                        for (; fieldResults.next(); fieldCount++) {
                            int fieldID = fieldResults.getInt(1);
                            String fieldName = fieldResults.getString(2);
                            form.addField(fieldName, fieldID);
                        }
                        logger.debug("Field Query returned {} records", fieldCount);

                        fieldResults.close();
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
