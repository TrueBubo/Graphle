package com.graphle.common.model

import com.graphle.file.model.Connection
import com.graphle.file.model.File
import com.graphle.fileWithTag.components.FileWithTag
import com.graphle.tag.model.Tag

data class DisplayedData(
    val filesWithTag: List<FileWithTag> = emptyList(),
    val filenames: List<String> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val connections: List<Connection> = emptyList(),
    val file: File? = null,
)