package com.apl.model;

import java.util.ArrayList;
import java.util.List;

public class Form {
    private final int id;
    private final int resolvedID;
    private final String name;
    private final List<Field> fieldList;

    public Form(String name, int id, int resolvedID) {
        this.name = name;
        this.id = id;
        this.resolvedID = resolvedID;
        this.fieldList = new ArrayList<>();
    }

    public void addField(String fieldName, Integer id) {
        Field field = new Field(fieldName, id);
        this.fieldList.add(field);
    }

    public Field getField(int fieldID) {
        for (Field field : this.fieldList) {
            if (field.getId() == fieldID) {
                return field;
            }
        }
        return null;
    }

    public Field getField(String fieldName) {
        for (Field field : this.fieldList) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public int getResolvedID() {
        return resolvedID;
    }

    public String getName() {
        return name;
    }

    public List<Field> getFieldList() {
        return fieldList;
    }
}
