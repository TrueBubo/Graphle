package com.graphle.graphlemanager.tag

import com.graphle.graphlemanager.file.AbsolutePathString
import kotlinx.serialization.Serializable

@Serializable
data class TagForFile(
    val location: AbsolutePathString,
    val tag: Tag,
)
