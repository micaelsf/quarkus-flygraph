package io.github.micaelsf.flygraph;

import lombok.NoArgsConstructor;
import org.eclipse.microprofile.config.ConfigProvider;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.SessionFactory;

@NoArgsConstructor
public class SessionFactoryBuilder {
    protected static final String[] PACKAGES = {
            "io.github.micaelsf.flygraph.nodes",
            "io.github.micaelsf.flygraph.relations",
    };
    private String uri;
    private String username;
    private String password;
    private String database;

    public SessionFactoryBuilder(String uri, String username, String password
            , String database) {
        this.uri = uri;
        this.username = username;
        this.password = password;
        this.database = database;
    }

    private String getUri() {
        return this.uri != null ? this.uri : ConfigProvider
                .getConfig()
                .getOptionalValue("flygraph.uri", String.class)
                .orElse("bolt://localhost:7687");
    }

    private String getDatabase() {
        return this.database != null ? this.database : ConfigProvider
                .getConfig()
                .getOptionalValue("flygraph.database", String.class)
                .orElse("neo4j");
    }

    private String getUsername() {
        return this.username != null ? this.username : ConfigProvider
                .getConfig()
                .getOptionalValue("flygraph.authentication.username", String.class)
                .orElse("neo4j");
    }

    private String getPassword() {
        return this.password != null ? this.password : ConfigProvider
                .getConfig()
                .getOptionalValue("flygraph.authentication.password", String.class)
                .orElse("");
    }

    protected SessionFactory getSessionFactory() {
        return new SessionFactory(new Configuration.Builder()
                .uri(getUri())
                .credentials(getUsername(), getPassword())
                .database(getDatabase())
                .useNativeTypes()
                .build(), PACKAGES);
    }
}
