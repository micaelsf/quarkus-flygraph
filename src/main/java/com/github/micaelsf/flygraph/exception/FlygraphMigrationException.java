package com.github.micaelsf.flygraph.exception;

public class FlygraphMigrationException extends RuntimeException {
    public FlygraphMigrationException(String message) {
        super("Migration failed: " + message);
    }
}
