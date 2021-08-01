package com.github.micaelsf.flygraph;

import com.github.micaelsf.Neo4jTestUtils;
import com.github.micaelsf.flygraph.nodes.FlygraphRoot;
import com.github.micaelsf.mocks.MockProfilePropertiesT1;
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

@TestProfile(MockProfilePropertiesT1.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTest
public class FlygraphTestT1 {

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
        flyGraph.runMigrations();
        var query = new StringJoiner("\n");
        query.add("MATCH (r:FlygraphRoot)-[rel]-(m:FlygraphMigration)");
        query.add("MATCH (m)-[rel1]-(q:FlygraphQuery)");
        query.add("RETURN r, rel, m, rel1, q");
        var it = getSession()
                .query(FlygraphRoot.class, query.toString(), Map.of());
        var root = it.iterator().next();
        assertNotNull(root);
        assertEquals(5, root.getMigrations().size());
        root.getMigrations().forEach(m -> {
            assertEquals(true, m.getExecuted());
            if ("V2.11.1__test".equals(m.getId()))
                assertEquals(3, m.getQueries().size());
            else
                assertEquals(1, m.getQueries().size());
        });
        assertEquals("2.11.1", root.getCurrentVersion());
        assertEquals("TestRoot1", root.getId());
    }

    @Test
    void testGetMigrationFilesOrder() {
        var migrationFiles = flyGraph.getMigrationFiles();

        assertTrue(migrationFiles.get(0).getName().contains("V2.0.1.1"));
        assertTrue(migrationFiles.get(1).getName().contains("V2.0.1.11"));
        assertTrue(migrationFiles.get(2).getName().contains("V2.0.1.11.9"));
        assertTrue(migrationFiles.get(3).getName().contains("V2.1.1.2"));
        assertTrue(migrationFiles.get(4).getName().contains("V2.11.1"));
    }
}
