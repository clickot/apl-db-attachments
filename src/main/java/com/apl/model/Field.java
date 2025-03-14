package com.apl.model;

public class Field {
    private final Integer id;
    private final String name;

    public Field(String name, int id) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }
}
