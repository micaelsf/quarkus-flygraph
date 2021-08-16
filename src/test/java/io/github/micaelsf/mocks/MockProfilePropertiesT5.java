package io.github.micaelsf.mocks;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class MockProfilePropertiesT5 implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("flygraph.uri", "bolt://localhost:7688",
                "flygraph.authentication.password", "xptoxpto",
                "flygraph.migration.start", "false",
                "flygraph.mode", "test",
                "flygraph.migration.path", "db/migrations/t5");
    }
}
