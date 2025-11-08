package com.graphle.graphlemanager.dsl

enum class TokenType {
    VARIABLE_NAME { override fun next() = OPERATOR },
    OPERATOR { override fun next() = VALUE },
    VALUE { override fun next() = CONJUNCTION },
    CONJUNCTION { override fun next() = VARIABLE_NAME };

    abstract fun next(): TokenType

    companion object {
        fun first() = VARIABLE_NAME
    }
}

enum class EntityType {
    File,
    Relationship
}
