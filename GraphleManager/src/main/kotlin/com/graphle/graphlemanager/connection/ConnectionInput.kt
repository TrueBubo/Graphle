package com.graphle.graphlemanager.connection

import com.graphle.graphlemanager.file.AbsolutePathString

data class ConnectionInput(
    val name: String,
    val value: String?,
    val locationFrom: AbsolutePathString,
    val locationTo: AbsolutePathString,
    val bidirectional: Boolean
)
