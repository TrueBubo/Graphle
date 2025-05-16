package com.graphle.graphlemanager.Connections

import com.graphle.graphlemanager.File.File
import org.springframework.lang.NonNull

@JvmRecord
data class NeighborConnection(
    @field:NonNull @param:NonNull val relationship: String,
    @field:NonNull @param:NonNull val toFiles: MutableList<File?>
)
