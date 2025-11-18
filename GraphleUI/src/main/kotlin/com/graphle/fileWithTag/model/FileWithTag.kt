package com.graphle.fileWithTag.model

import com.graphle.tag.model.Tag
import kotlinx.serialization.Serializable

@Serializable
data class FileWithTag(
    val location: String,
    val tag: Tag
)