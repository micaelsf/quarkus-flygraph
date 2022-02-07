package io.github.micaelsf.flygraph;

import io.github.micaelsf.Neo4jTestUtils;
import io.github.micaelsf.flygraph.nodes.FlygraphRoot;
import io.github.micaelsf.mocks.MockProfilePropertiesT6;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.ogm.session.Session;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;
import java.util.StringJoiner;

import static org.junit.jupiter.api.Assertions.*;

@TestProfile(MockProfilePropertiesT6.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTest
public class FlygraphTestT6 {

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
        assertEquals(2, root.getMigrations().size());
        root.getMigrations().forEach(m -> {
            assertEquals(true, m.getExecuted());
            assertEquals(1, m.getQueries().size());
        });
        assertEquals("2.111.1.999", root.getCurrentVersion());
    }

    @Test
    void testGetMigrationFilesOrder() throws IOException {
        var migrationFiles = flyGraph.getMigrationFiles();

        assertTrue(migrationFiles.get(0).getName().contains("V2.1.1.101"));
        assertTrue(migrationFiles.get(1).getName().contains("V2.111.1.999"));
    }
}
