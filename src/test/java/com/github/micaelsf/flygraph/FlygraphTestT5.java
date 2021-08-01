package com.github.micaelsf.flygraph;

import com.github.micaelsf.Neo4jTestUtils;
import com.github.micaelsf.flygraph.nodes.FlygraphRoot;
import com.github.micaelsf.mocks.MockProfilePropertiesT5;
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

@TestProfile(MockProfilePropertiesT5.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTest
public class FlygraphTestT5 {

    @Inject
    FlyGraph flyGraph;

    @BeforeAll
    public void startup() {
        dropAndCreate();
    }

    public void dropAndCreate() {
        var sessionFactoryBuilder = new SessionFactoryBuilder();
        Neo4jTestUtils.dropAndCreate(sessionFactoryBuilder.getSessionFactory(), "flygraphT5");
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
            assertEquals("Migration V2.0.1.1__test " +
                    "has a different checksum, please create a new migration " +
                    "to apply new changes!", e.getMessage());
        }
        var query = new StringJoiner("\n");
        query.add("MATCH (r:FlygraphRoot)-[rel]-(m:FlygraphMigration)");
        query.add("MATCH (m)-[rel1]-(q:FlygraphQuery)");
        query.add("RETURN r, rel, m, rel1, q");
        var it = getSession()
                .query(FlygraphRoot.class, query.toString(), Map.of());
        var root = it.iterator().next();
        assertNotNull(root);
        assertEquals(1, root.getMigrations().size());
        root.getMigrations().forEach(m -> {
            assertEquals(true, m.getExecuted());
            assertEquals(1, m.getQueries().size());
        });
        assertEquals("2.0.1.1", root.getCurrentVersion());
    }
}
