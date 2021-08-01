package com.github.micaelsf.flygraph.exception;

public class FlygraphMigrationVersionTooOld extends RuntimeException {
    public FlygraphMigrationVersionTooOld(String oldVersion, String lastVersion) {
        super(String.format("Migration version '%s' is too old. Please create a" +
                        " migration with a version greater than the latest '%s'",
                oldVersion, lastVersion));
    }
}
