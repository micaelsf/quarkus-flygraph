package com.github.micaelsf.flygraph.exception;

public class FlygraphMigrationFilenameException extends RuntimeException {
    public FlygraphMigrationFilenameException(int versionHouses,
                                              String versionSeparator) {
        super(String.format("Migration filename is not well formatted.\n" +
                " - migration file version must have a maximum of %s " +
                "decimal houses separated by a '%s'\n" +
                " - version and the description must be separated by double " +
                "underscore.\n" +
                " - description must only contain alphanumeric " +
                "characters separated by - or _ and ends with the '.cypher' " +
                "extension", versionHouses, versionSeparator));
    }
}
