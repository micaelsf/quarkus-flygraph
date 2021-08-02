package io.github.micaelsf.flygraph;

import io.github.micaelsf.flygraph.exception.*;
import io.github.micaelsf.flygraph.nodes.FlygraphMigration;
import io.github.micaelsf.flygraph.nodes.FlygraphQuery;
import io.github.micaelsf.flygraph.nodes.FlygraphRoot;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.session.Session;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.github.micaelsf.flygraph.Utils.*;
import static java.lang.String.format;

@ApplicationScoped
public class FlyGraph {

    @ConfigProperty(name = "flygraph.migration.path", defaultValue = "db/migration")
    String migrationPath;

    @ConfigProperty(name = "flygraph.version.valid.houses", defaultValue = "2")
    int versionHouses;

    @ConfigProperty(name = "flygraph.version.separator", defaultValue = ".")
    String versionSeparator;

    @ConfigProperty(name = "flygraph.version.baseline", defaultValue = "1.0.0.0")
    String versionBaseline;

    @ConfigProperty(name = "flygraph.root.node", defaultValue = "default")
    String rootNode;

    @Inject
    Logger logger;

    private Session openSession() {
        var sessionFactoryBuilder = new SessionFactoryBuilder();
        return sessionFactoryBuilder.getSessionFactory().openSession();
    }

    public void runMigrations() {
        var session = openSession();
        var totalRanMigrations = 0;
        var successFulMigrations = 0;
        logger.info(format("Searching for migrations at '%s'",
                getSrcMigrationPath(migrationPath)));
        try {
            var root = queryRootNode(session);
            List<File> files = getMigrationFiles();

            if (root.getMigrations() == null)
                root.setMigrations(new ArrayList<>());

            List<FlygraphMigration> migrations = queryAllMigrations();

            List<File> newMigrationFiles = checkMigrations(files, migrations);
            totalRanMigrations = newMigrationFiles.size();
            var i = 0;
            for (File f : newMigrationFiles) {
                var migration = executeFile(root, Path.of(f.getPath()));
                if (migration != null)
                    successFulMigrations++;

                if (i == newMigrationFiles.size() - 1) {
                    root.setCurrentVersion(getVersionFrom(
                            getMigrationName(f.getPath(), migrationPath)));
                    session.save(root, 0);
                }

                i++;
            }

        } catch (ClientException e) {
            if (e.code().equals("Neo.ClientError.Statement.SyntaxError")) {
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        if (totalRanMigrations == 0)
            logger.info("Found nothing to migrate");
        else
            logger.info(format("%s New Migrations applied!" +
                            "\tSuccessful: %s" +
                            "\tFailure: %s",
                    totalRanMigrations,
                    successFulMigrations,
                    totalRanMigrations - successFulMigrations));
    }

    private List<File> checkMigrations(List<File> files,
                                       List<FlygraphMigration> migrations) {
        if (migrations.isEmpty())
            return files;

        List<File> newMigrationFiles = new ArrayList<>();
        files.forEach(f -> {
            var migrated = false;

            for (var m : migrations) {
                var migrationName = getMigrationName(f.getPath(), migrationPath);
                if (m.getId().equals(migrationName)) {
                    logger.debug(format("%s is already migrated", migrationName));
                    migrated = true;
                    try {
                        var path = Path.of(f.getPath());
                        String fileContent = Files.readString(path);
                        var fileChecksum = computeChecksum(fileContent);
                        if (!m.getChecksum().equals(fileChecksum)) {
                            throw new FlygraphChecksumException(migrationName);
                        }
                    } catch (IOException e) {
                        throw new FlygraphReadMigrationException(e.getMessage());
                    }
                    break;
                }
            }

            if (!migrated)
                newMigrationFiles.add(f);
        });

        return newMigrationFiles;
    }

    protected List<File> getMigrationFiles() throws FlygraphMigrationFilenameException {
        var datasetPath = getSrcMigrationPath(migrationPath);
        var file = new File(datasetPath);
        File[] files = file.listFiles();
        if (files == null)
            return new ArrayList<>();
        for (File f : files) {
            var migrationFile = getMigrationFileName(f.getName(), migrationPath);
            var regex = "V([0-9]{1," + versionHouses + "})(" +
                    (versionSeparator.equals(".") ? "\\." : versionSeparator) +
                    "[0-9]{1," + versionHouses + "})*__[\\w\\d-_]*.cypher";
            if (!Pattern.matches(regex, migrationFile))
                throw new FlygraphMigrationFilenameException(versionHouses, versionSeparator);
        }

        if (files.length == 1)
            buildFullVersionFromFile(files[0], versionSeparator,
                    versionHouses, migrationPath);

        var distinct = new ArrayList<>();
        Arrays.stream(files).forEach(f -> {
            var v = getVersionFrom(
                    getMigrationName(f.getPath(), migrationPath));
            if (distinct.stream().anyMatch(x -> x.equals(v)))
                throw new FlygraphDuplicatedMigrationException(v);
            distinct.add(v);
        });

        return Arrays.stream(files)
                .sorted(Comparator.comparing(f -> buildFullVersionFromFile(
                        f, versionSeparator, versionHouses, migrationPath)))
                .collect(Collectors.toList());
    }

    protected FlygraphMigration executeFile(FlygraphRoot root, Path fileName)
            throws IOException {
        var session = openSession();
        var migrationName =
                getMigrationName(fileName.toFile().getName(), migrationPath);
        var thisVersion = getMigrationName(fileName.toFile().getPath(),
                migrationPath);
        verifyVersionGreaterThanCurrentOrThrow(versionSeparator, versionHouses,
                root.getCurrentVersion(), getVersionFrom(thisVersion));

        logger.info(format("Running new migration %s ...", migrationName));
        String query;
        FlygraphMigration flyGraphMigration = null;
        boolean applied = false;

        try {
            query = Files.readString(fileName);

            if (!query.isBlank()) {
                applied = true;

                Set<FlygraphQuery> createdQueries = new HashSet<>();
                for (String q : query.split(";")) {
                    q = q.replaceAll("\\s+", " ");
                    if (!q.isBlank()) {
                        // execute the migration query
                        session.query(q, Map.of());
                        // add flygraph query migration node
                        createdQueries.add(createFlyGraphQuery(session, q));
                    }
                }

                // create migration node and link all query nodes
                flyGraphMigration = createFlyGraphMigration(
                        session, root, query, migrationName);
                flyGraphMigration.setQueries(createdQueries);
                session.save(flyGraphMigration);
            }
        } catch (ClientException e) {
            if (flyGraphMigration != null) {
                flyGraphMigration.setExecuted(false);
                session.save(flyGraphMigration);
            }
            throw e;
        }

        logger.info(format(applied ? "[SUCCESS] %s applied!" :
                "[FAIL] %s not applied!", migrationName));
        return flyGraphMigration;
    }

    protected FlygraphRoot queryRootNode(Session session) {
        Filter id = new Filter("id", ComparisonOperator.EQUALS, rootNode);
        var root =
                session.loadAll(FlygraphRoot.class, id);
        if (root.isEmpty()) {
            var regex = "[0-9" + versionSeparator + "]+";
            if (!Pattern.matches(regex, versionBaseline))
                throw new FlygraphBaselineVersionException(versionBaseline);

            var flygraphRoot = new FlygraphRoot();
            flygraphRoot.setId(rootNode);
            flygraphRoot.setCurrentVersion(versionBaseline);
            session.save(flygraphRoot, 0);
            logger.info(format("Create migration root with baseline %s",
                    versionBaseline));
            return flygraphRoot;
        }
        return root.iterator().next();
    }

    protected List<FlygraphMigration> queryAllMigrations() {
        var session = openSession();
        Collection<FlygraphMigration> flygraphMigrations =
                session.loadAll(FlygraphMigration.class);
        return new ArrayList<>(flygraphMigrations);
    }

    protected FlygraphMigration createFlyGraphMigration(
            Session session, FlygraphRoot root, String query,
            String migrationId) {
        var flyGraphMigration = new FlygraphMigration();
        flyGraphMigration.setId(migrationId);
        flyGraphMigration.setChecksum(computeChecksum(query));
        flyGraphMigration.setExecuted(true);
        flyGraphMigration.setRoot(root);
        session.save(flyGraphMigration);
        return flyGraphMigration;
    }

    protected FlygraphQuery createFlyGraphQuery(Session session, String query) {
        var flyGraphQuery = new FlygraphQuery();
        flyGraphQuery.setQuery(query);
        flyGraphQuery.setExecutedAt(LocalDateTime.now());
        session.save(flyGraphQuery);
        return flyGraphQuery;
    }
}
