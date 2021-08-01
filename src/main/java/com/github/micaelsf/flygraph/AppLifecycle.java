package com.github.micaelsf.flygraph;

import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
class AppLifecycle {

    @ConfigProperty(name = "flygraph.migrate-at-start", defaultValue = "true")
    boolean migrateAtStart;
    @Inject
    FlyGraph flyGraph;

    void onStart(@Observes StartupEvent ev) {
        if (migrateAtStart)
            flyGraph.runMigrations();
    }
}