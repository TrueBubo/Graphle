package com.graphle

data class Connection(
    val to: String,
    val name: String,
    val value: String? = null
)