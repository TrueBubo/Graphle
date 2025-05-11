package com.graphle.graphlemanager.File;

import com.graphle.graphlemanager.Connections.RangedNeighborConnections;
import com.graphle.graphlemanager.Tag.Tag;
import org.springframework.lang.NonNull;

import java.util.List;

public record File(@NonNull String location, int updated, @NonNull List<Tag> tags,
                   @NonNull RangedNeighborConnections connections) {
}
