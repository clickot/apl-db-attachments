package com.apl.db;

import com.apl.model.Field;
import com.apl.model.Form;

import java.util.List;
import java.util.Map;

public class Queries {

    static String getAttachmentBytesQuery(int formID, int fieldID, String entryID) {
        return "select c" + fieldID + " from b" + formID + "c" + entryID + " where entryID = '" + entryID + "'";
    }

    static String getFormDataQuery(List<String> formNameList) {
        String query = "select distinct a.name, a.schemaid, a.resolvedschemaid from arschema a inner join field f on a.schemaid = f.schemaId inner join field_attach fa on a.schemaId = fa.schemaId and f.fieldId = fa.fieldId where a.schemaType = 1 and f.fOption < 4";
        if (formNameList != null && !formNameList.isEmpty()) {
            query += (" and a.name in (" + String.join(",", formNameList) + ")");
        }
        return query + " order by a.name";
    }

    public static String getAttachmentRecordQuery(Map<String, Form> formMap, String formName, String fieldName, String whereClause) {
        if (formName == null || formName.isEmpty() || fieldName == null || fieldName.isEmpty()) {
            throw new IllegalArgumentException("formName and fieldName must not be null or empty");
        }
        Form form = formMap.get(formName);
        if (form != null) {
            int formID = form.getResolvedID();
            if (formID > 0) {
                Field field = form.getField(fieldName);
                if (field != null) {
                    int fieldID = field.getId();
                    if (fieldID > 0) {
                        String query = "select b.c1 AttReqID, b.c" + fieldID + " AttFileName, b.co" + fieldID + " AttSize from b" + formID + " b inner join t" + formID + " t on t.c1 = b.c1";
                        if (whereClause != null && !whereClause.trim().isEmpty()) {
                            query += " where " + whereClause;
                        }
                        return query;
                    }
                }
            }
        }
        return null;
    }

    static String getFieldQuery(String formName) {
        return "select distinct f.fieldID, f.fieldName from arschema a inner join field f on a.schemaid = f.schemaId inner join field_attach fa on a.schemaId = fa.schemaId and f.fieldId = fa.fieldId where a.name = '" + formName + "' and f.fOption < 4 order by f.fieldName";
    }
}