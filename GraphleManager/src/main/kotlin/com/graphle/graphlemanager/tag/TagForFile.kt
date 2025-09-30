package com.graphle.graphlemanager.tag

import com.graphle.graphlemanager.file.AbsolutePathString

data class TagForFile(
    val location: AbsolutePathString,
    val tag: Tag,
)
