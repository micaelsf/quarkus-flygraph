package io.github.micaelsf.flygraph.exception;

public class FlygraphDuplicatedMigrationException extends RuntimeException {
    public FlygraphDuplicatedMigrationException(String version) {
        super("Duplicated migration version: " + version);
    }
}
