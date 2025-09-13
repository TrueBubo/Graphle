package com.graphle.graphlemanager.connection

import com.graphle.graphlemanager.file.AbsolutePathString

data class Connection(
    val name: String,
    val value: String? = null,
    val from: AbsolutePathString,
    val to: AbsolutePathString,
    val bidirectional: Boolean? = null
)