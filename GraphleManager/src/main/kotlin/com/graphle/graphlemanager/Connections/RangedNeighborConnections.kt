package com.graphle.graphlemanager.Connections

import com.graphle.graphlemanager.Time.TimeRange

data class RangedNeighborConnections(
    val range: TimeRange,
    val neighborConnections: List<NeighborConnection>
)
