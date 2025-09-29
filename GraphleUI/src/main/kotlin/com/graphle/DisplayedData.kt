package com.graphle

data class DisplayedData(
    val filesWithTag: List<FileWithTag> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val connections: List<Connection> = emptyList()
)