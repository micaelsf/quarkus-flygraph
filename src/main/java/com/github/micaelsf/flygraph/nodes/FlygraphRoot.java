package com.github.micaelsf.flygraph.nodes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

import java.util.List;

@Data
@NodeEntity
public class FlygraphRoot {

    @Id
    private String id;

    @Property
    private String currentVersion;

    @Relationship(value = "MIGRATED_WITHIN", direction = "INCOMING")
    @EqualsAndHashCode.Exclude
    private List<FlygraphMigration> migrations;
}
