package com.graphle.graphlemanager.File;

import com.graphle.graphlemanager.Connections.RangedNeighborConnections;
import com.graphle.graphlemanager.Tag.Tag;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.UUID;

public record File(@Id @GeneratedValue UUID id, @NonNull String location, @NonNull Long updated, @NonNull List<Tag> tags,
                   @NonNull RangedNeighborConnections connections) {
    public File(@NonNull String location, Long updated, List<Tag> tags, RangedNeighborConnections connections) {
        this(null, location,  updated,  tags != null ? tags : List.of(), connections);
    }
}
