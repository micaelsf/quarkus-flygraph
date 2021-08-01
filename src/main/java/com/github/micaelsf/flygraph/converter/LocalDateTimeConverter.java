package com.github.micaelsf.flygraph.converter;

import org.neo4j.ogm.typeconversion.AttributeConverter;

import java.time.LocalDateTime;

public class LocalDateTimeConverter
        implements AttributeConverter<LocalDateTime, String> {

    @Override
    public String toGraphProperty(LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.toString();
    }

    @Override
    public LocalDateTime toEntityAttribute(String s) {
        return s == null ? null : LocalDateTime.parse(s);
    }
}
