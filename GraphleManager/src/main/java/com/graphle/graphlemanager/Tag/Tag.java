package com.graphle.graphlemanager.Tag;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.UUID;

public record Tag(@Id @GeneratedValue UUID id, @NonNull String name, @Nullable String value, @Nullable Double numericValue) {
    public Tag(@NonNull String name, @Nullable String value, @Nullable Double numericValue) {
        this(null, name, value, numericValue);
    }
}
