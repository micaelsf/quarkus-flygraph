package com.github.micaelsf.flygraph;

import lombok.NoArgsConstructor;
import org.eclipse.microprofile.config.ConfigProvider;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.SessionFactory;

@NoArgsConstructor
public class SessionFactoryBuilder {
    protected static final String[] PACKAGES = {
            "com.github.micaelsf.flygraph.nodes",
            "com.github.micaelsf.flygraph.relations",
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
                .getValue("flygraph.uri", String.class);
    }

    private String getDatabase() {
        return this.database != null ? this.database : ConfigProvider
                .getConfig()
                .getValue("flygraph.database", String.class);
    }

    private String getUsername() {
        return this.username != null ? this.username : ConfigProvider
                .getConfig()
                .getValue("flygraph.authentication.username", String.class);
    }

    private String getPassword() {
        return this.password != null ? this.password : ConfigProvider
                .getConfig()
                .getValue("flygraph.authentication.password", String.class);
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
