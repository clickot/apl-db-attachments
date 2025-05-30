package com.apl.db;

public enum DBType {

    ORACLE("Oracle"), SQL_SERVER("SQL Server"), POSTGRESQL("PostgreSQL");

    private final String displayname;

    DBType(String displayname) {
        this.displayname = displayname;
    }

    public final String getDisplayname() {
        return displayname;
    }
}
