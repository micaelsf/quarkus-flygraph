package io.github.micaelsf.flygraph.exception;

public class FlygraphConfigModeException extends RuntimeException {
    public FlygraphConfigModeException() {
        super("Invalid configuration 'flygraph.mode'. Available options " +
                "[prod, test]");
    }
}
