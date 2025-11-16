package com.graphle.file.model

import com.graphle.tag.model.Tag
import kotlinx.serialization.Serializable

@Serializable
data class File(
    val location: String,
    val tags: List<Tag>,
    val connections: List<Connection>
)