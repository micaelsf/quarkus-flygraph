package io.github.micaelsf.flygraph.exception;

public class FlygraphChecksumException extends RuntimeException {
    public FlygraphChecksumException(String migration) {
        super(String.format("Migration %s has a different " +
                "checksum, please create a new migration " +
                "to apply new changes!", migration));
    }
}
