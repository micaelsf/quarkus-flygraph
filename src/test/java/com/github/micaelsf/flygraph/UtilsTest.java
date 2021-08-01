package com.github.micaelsf.flygraph;


import com.github.micaelsf.flygraph.exception.FlygraphMigrationVersionTooOld;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class UtilsTest {

    @Test
    void testComputeChecksum() {
        var test1 = "test";
        var test2 = "t3st";
        var test3 = "tests";

        String checksum = Utils.computeChecksum(test1);
        assertEquals(8, checksum.length());
        assertNotEquals(checksum, Utils.computeChecksum(test2));
        assertNotEquals(checksum, Utils.computeChecksum(test3));
    }

    @Test
    void testGetMigrationFileName() {
        var fullPath = "/full/path/of/migration/V1.0__migration.cypher";

        var filename = Utils.getMigrationFileName(fullPath, "migration");
        assertEquals("V1.0__migration.cypher", filename);

        filename = Utils.getMigrationFileName(fullPath, "migration/");
        assertEquals("V1.0__migration.cypher", filename);

        filename = Utils.getMigrationFileName(fullPath, "unknown");
        assertEquals(fullPath, filename);
    }

    @Test
    void testGetMigrationName() {
        var fullPath = "/full/path/of/migration/V1.0__migration.cypher";

        var filename = Utils.getMigrationName(fullPath, "migration");
        assertEquals("V1.0__migration", filename);

        filename = Utils.getMigrationName(fullPath, "migration/");
        assertEquals("V1.0__migration", filename);

        filename = Utils.getMigrationName(fullPath, "unknown");
        assertEquals("/full/path/of/migration/V1.0__migration", filename);
    }

    @Test
    void testGetVersionFrom() {
        var migration = "V1.0__migration.cypher";
        var migration1 = "V1.0.9.12__migration.cypher";
        var migration2 = "V1.0.9.a__migration.cypher";

        var v = Utils.getVersionFrom(migration);
        assertEquals("1.0", v);

        v = Utils.getVersionFrom(migration1);
        assertEquals("1.0.9.12", v);

        v = Utils.getVersionFrom(migration2);
        assertEquals("1.0.9.a", v);
    }

    @Test
    void testBuildFullVersion() {
        var separatorConfig = ".";
        var maxHousesConfig = 2;
        var v1 = "1.1";
        var v2 = "1.1.11";
        var fail = "1.0.123.1.11";

        var f1 = Utils.buildFullVersion(v1, separatorConfig, maxHousesConfig);
        assertEquals("01.01", f1);
        var f2 = Utils.buildFullVersion(v2, separatorConfig, maxHousesConfig);
        assertEquals("01.01.11", f2);

        maxHousesConfig = 3;

        f1 = Utils.buildFullVersion(v1, separatorConfig, maxHousesConfig);
        assertEquals("001.001", f1);
        f2 = Utils.buildFullVersion(v2, separatorConfig, maxHousesConfig);
        assertEquals("001.001.011", f2);
        var f3 = Utils.buildFullVersion(fail, separatorConfig, maxHousesConfig);
        assertEquals("001.000.123.001.011", f3);
    }

    @Test
    void testBuildFullVersionFromFile() {
        var f = new File("/full/path/of/migration/V1.1__migration.cypher");
        var fullVersion = Utils.buildFullVersionFromFile(
                f, ".", 2, "migration");
        assertEquals("01.01", fullVersion);
    }

    @Test
    void testVerifyVersionGreaterThanCurrentOrThrow() {
        try {
            Utils.verifyVersionGreaterThanCurrentOrThrow(
                    ".", 2, "1.0.0.0", "1.0.0.1");
            assertTrue(true);
        } catch (FlygraphMigrationVersionTooOld e) {
            fail();
        }

        try {
            Utils.verifyVersionGreaterThanCurrentOrThrow(
                    ".", 2, "1.11.0.0", "1.10.99.99");
            fail();
        } catch (FlygraphMigrationVersionTooOld e) {
            assertTrue(true);
        }
    }
}
