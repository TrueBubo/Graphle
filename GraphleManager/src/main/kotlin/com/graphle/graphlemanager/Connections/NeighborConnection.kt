package com.graphle.graphlemanager.Connections

import com.graphle.graphlemanager.File.File

data class NeighborConnection(
    val relationship: String,
    val toFiles: List<File>
)
