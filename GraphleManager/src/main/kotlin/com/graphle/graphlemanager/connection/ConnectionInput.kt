package com.graphle.graphlemanager.connection

import com.graphle.graphlemanager.file.AbsolutePathString

data class ConnectionInput(
    val name: String,
    val value: String?,
    val from: AbsolutePathString,
    val to: AbsolutePathString,
    val bidirectional: Boolean
)
