package com.graphle

import com.graphle.type.Connection

data class File(
    val location: String,
    val tags: List<Tag>,
    val connections: List<Connection>
)
