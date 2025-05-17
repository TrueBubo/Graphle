package com.graphle.graphlemanager.connections

import com.graphle.graphlemanager.time.TimeRange

data class RangedNeighborConnections(
    val range: TimeRange,
    val neighborConnections: List<NeighborConnection>
)
