package io.github.micaelsf.flygraph.exception;

public class FlygraphReadMigrationException extends RuntimeException {
    public FlygraphReadMigrationException(String message) {
        super("Migration file read failure: " + message);
    }
}
