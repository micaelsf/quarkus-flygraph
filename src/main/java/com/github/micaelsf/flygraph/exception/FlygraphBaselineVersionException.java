package com.github.micaelsf.flygraph.exception;

public class FlygraphBaselineVersionException extends RuntimeException {
    public FlygraphBaselineVersionException(String version) {
        super(String.format("Invalid baseline version %s ", version));
    }
}
