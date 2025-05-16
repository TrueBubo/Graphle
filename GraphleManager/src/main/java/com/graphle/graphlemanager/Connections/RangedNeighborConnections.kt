package com.graphle.graphlemanager.Connections

import com.graphle.graphlemanager.Time.TimeRange
import org.springframework.lang.NonNull

@JvmRecord
data class RangedNeighborConnections(
    @field:NonNull @param:NonNull val range: TimeRange,
    @field:NonNull @param:NonNull val neighborConnections: List<NeighborConnection?>
)
