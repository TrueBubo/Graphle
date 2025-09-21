package com.graphle

data class DisplayedData(
    val tags: List<Tag> = emptyList(),
    val connections: List<Connection> = emptyList()
)