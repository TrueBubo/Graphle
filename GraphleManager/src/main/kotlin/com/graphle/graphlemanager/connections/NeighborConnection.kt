package com.graphle.graphlemanager.connections

import com.graphle.graphlemanager.file.File

data class NeighborConnection(
    val relationship: String,
    val toFiles: List<File>
)
