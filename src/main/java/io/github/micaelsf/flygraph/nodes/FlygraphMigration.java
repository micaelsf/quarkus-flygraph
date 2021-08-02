package io.github.micaelsf.flygraph.nodes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

@Data
@NodeEntity
public class FlygraphMigration {

    @Id
    private String id;

    @Property
    private String checksum;

    @Property
    private Boolean executed;

    @Relationship(value = "EXECUTED_WITHIN", direction = "INCOMING")
    @EqualsAndHashCode.Exclude
    private Set<FlygraphQuery> queries;

    @Relationship(value = "MIGRATED_WITHIN")
    @EqualsAndHashCode.Exclude
    private FlygraphRoot root;
}
