package com.graphle.graphlemanager.Connections;

import com.graphle.graphlemanager.Time.TimeRange;
import org.springframework.lang.NonNull;

import java.util.List;

public record RangedNeighborConnections(@NonNull TimeRange range,
                                        @NonNull List<NeighborConnection> neighborConnections) {
}
