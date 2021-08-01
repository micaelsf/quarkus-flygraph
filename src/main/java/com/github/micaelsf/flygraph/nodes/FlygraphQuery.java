package com.github.micaelsf.flygraph.nodes;

import com.github.micaelsf.flygraph.converter.LocalDateTimeConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.Convert;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@NodeEntity
public class FlygraphQuery {

    @Id
    private Instant id = Instant.now();

    @Property
    private String query;

    @Property
    @Convert(LocalDateTimeConverter.class)
    private LocalDateTime executedAt;

    @Relationship("EXECUTED_WITHIN")
    @EqualsAndHashCode.Exclude
    private FlygraphMigration migration;
}
