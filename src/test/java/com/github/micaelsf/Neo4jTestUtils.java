package com.github.micaelsf;

import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class Neo4jTestUtils {

    private Neo4jTestUtils() {
    }

    public static void dropAndCreate(SessionFactory sessionfactory,
                                     String dataset) {
        Session session = sessionfactory.openSession();
        session.purgeDatabase();
        String datasetPath = String.format(
                "./src/test/resources/datasets/%s.cypher", dataset);
        try {
            Path fileName = Path.of(datasetPath);
            String query = Files.readString(fileName);
            for (String q : query.split(";")) {
                if (!q.isBlank())
                    session.query(q, Map.of());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
