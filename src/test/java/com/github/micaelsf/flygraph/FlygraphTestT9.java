package com.github.micaelsf.flygraph;

import com.github.micaelsf.Neo4jTestUtils;
import com.github.micaelsf.flygraph.nodes.FlygraphRoot;
import com.github.micaelsf.mocks.MockProfilePropertiesT9;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.ogm.session.Session;

import javax.inject.Inject;
import java.util.Map;
import java.util.StringJoiner;

import static org.junit.jupiter.api.Assertions.*;

@TestProfile(MockProfilePropertiesT9.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTest
public class FlygraphTestT9 {

    @Inject
    FlyGraph flyGraph;

    @BeforeAll
    public void startup() {
        dropAndCreate();
    }

    public void dropAndCreate() {
        var sessionFactoryBuilder = new SessionFactoryBuilder();
        Neo4jTestUtils.dropAndCreate(sessionFactoryBuilder.getSessionFactory(), "flygraphT9");
    }

    public Session getSession() {
        var sessionFactoryBuilder = new SessionFactoryBuilder();
        return sessionFactoryBuilder.getSessionFactory().openSession();
    }

    @Test
    void testRunMigration() {
        try {
            flyGraph.runMigrations();
            fail();
        } catch (RuntimeException e) {
            assertTrue(true);
            assertEquals("Migration version '2.0.99.99' is too old. " +
                    "Please create a migration with a version greater than " +
                    "the latest '2.1.0.0'", e.getMessage());
        }
        var query = new StringJoiner("\n");
        query.add("MATCH (r:FlygraphRoot)");
        query.add("RETURN r LIMIT 1");
        var it = getSession()
                .query(FlygraphRoot.class, query.toString(), Map.of());
        var root = it.iterator().hasNext() ? it.iterator().next() : null;
        assertEquals("2.1.0.0", root.getCurrentVersion());
    }
}
