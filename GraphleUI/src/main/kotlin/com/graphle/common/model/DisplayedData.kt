package com.graphle.common.model

import com.graphle.file.model.Connection
import com.graphle.file.model.File
import com.graphle.fileWithTag.model.FileWithTag
import com.graphle.tag.model.Tag

/**
 * Data container for different types of displayable content.
 *
 * @property location Current file location path
 * @property filesWithTag List of files associated with a specific tag
 * @property filenames List of filename strings
 * @property tags List of tags associated with a file
 * @property connections List of file relationships/connections
 */
data class DisplayedData(
    val location: String? = null,
    val filesWithTag: List<FileWithTag> = emptyList(),
    val filenames: List<String> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val connections: List<Connection> = emptyList(),
)