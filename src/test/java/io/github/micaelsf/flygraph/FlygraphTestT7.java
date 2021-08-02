package io.github.micaelsf.flygraph;

import io.github.micaelsf.Neo4jTestUtils;
import io.github.micaelsf.flygraph.nodes.FlygraphRoot;
import io.github.micaelsf.mocks.MockProfilePropertiesT7;
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

@TestProfile(MockProfilePropertiesT7.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTest
public class FlygraphTestT7 {

    @Inject
    FlyGraph flyGraph;

    @BeforeAll
    public void startup() {
        dropAndCreate();
    }

    public void dropAndCreate() {
        var sessionFactoryBuilder = new SessionFactoryBuilder();
        Neo4jTestUtils.dropAndCreate(sessionFactoryBuilder.getSessionFactory(), "flygraph");
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
            assertEquals("Duplicated migration version: 2.0.1.1", e.getMessage());
        }
        var query = new StringJoiner("\n");
        query.add("MATCH (r:FlygraphRoot)");
        query.add("RETURN r LIMIT 1");
        var it = getSession()
                .query(FlygraphRoot.class, query.toString(), Map.of());
        var root = it.iterator().hasNext() ? it.iterator().next() : null;
        assertNotNull(root);
    }
}
