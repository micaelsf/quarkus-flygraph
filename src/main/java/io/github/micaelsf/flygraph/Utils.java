package io.github.micaelsf.flygraph;

import io.github.micaelsf.flygraph.exception.FlygraphConfigModeException;
import io.github.micaelsf.flygraph.exception.FlygraphMigrationVersionTooOld;
import io.quarkus.runtime.configuration.ProfileManager;
import org.eclipse.microprofile.config.ConfigProvider;

import java.io.File;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import static java.lang.String.format;

public class Utils {

    private Utils() {
    }

    protected static String computeChecksum(String query) {
        byte[] bytes = query.getBytes();
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return Long.toHexString(crc32.getValue()).toUpperCase();
    }

    protected static String getMigrationFileName(String fullPath,
                                                 String migrationPath) {
        var splitToken = migrationPath;
        var lastChar = migrationPath.charAt(migrationPath.length() - 1);
        if (lastChar != '/')
            splitToken = splitToken + "/";
        String[] splitFilename = fullPath.split(splitToken);
        return splitFilename[splitFilename.length - 1];
    }

    protected static String getMigrationName(String fullPath,
                                             String migrationPath) {
        return getMigrationFileName(fullPath, migrationPath)
                .split(".cypher")[0];
    }

    protected static String getVersionFrom(String migrationName) {
        var noV = migrationName.substring(1);
        return noV.split("__")[0];
    }

    protected static String buildFullVersion(String versionStr,
                                             String versionSeparator,
                                             int versionHouses) {
        var fullVersion = new StringJoiner(versionSeparator);
        String[] versionParts = ".".equals(versionSeparator) ?
                versionStr.split("\\.") :
                versionStr.split(versionSeparator);

        for (var p : versionParts) {
            var format = "%0" + versionHouses + "d";
            fullVersion.add(String.format(format, Integer.parseInt(p)));
        }
        return fullVersion.toString();
    }

    protected static String buildFullVersionFromFile(
            File file, String versionSeparator, int versionHouses,
            String migrationPath) {
        var version = getVersionFrom(
                getMigrationName(file.getPath(), migrationPath));
        return buildFullVersion(version, versionSeparator,
                versionHouses);
    }

    protected static void verifyVersionGreaterThanCurrentOrThrow(
            String versionSeparator, int versionHouses, String currentVersion,
            String version) {
        var migrationVersion = buildFullVersion(version,
                versionSeparator,
                versionHouses);
        var lastVersion = buildFullVersion(currentVersion,
                versionSeparator,
                versionHouses);
        if (migrationVersion.compareToIgnoreCase(lastVersion) < 0)
            throw new FlygraphMigrationVersionTooOld(version, currentVersion);
    }

    protected static String getSrcMigrationPath(String migrationPath) {
        String mode = ConfigProvider.getConfig()
                .getOptionalValue("flygraph.mode", String.class)
                .orElse("prod").toLowerCase();
        if (!Arrays.asList("test", "prod").contains(mode))
            throw new FlygraphConfigModeException();

        var activeProfile = ProfileManager.getActiveProfile();
        return buildMigrationPath(migrationPath, mode, activeProfile);
    }

    protected static String buildMigrationPath(String migrationPath,
                                               String mode,
                                               String activeProfile) {
        var pathPrefix = "";
        if (!(activeProfile.equals("dev") || activeProfile.equals("prod"))) {
            var env = "main";
            if ("test".equals(mode))
                env = activeProfile.equals("test") ||
                        activeProfile.equals("localtest") ?
                        "test" : "main";
            pathPrefix = format("src/%s/resources/", env);
        } else if (activeProfile.equals("dev")) {
            pathPrefix = "classes/";
        }
        return format("%s%s", pathPrefix, migrationPath);
    }
}
