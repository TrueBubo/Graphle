package com.graphle.graphlemanager.dsl

/**
 * Represents the type of token in a DSL query.
 * Tokens follow a specific order: VARIABLE_NAME -> OPERATOR -> VALUE -> CONJUNCTION -> VARIABLE_NAME.
 */
enum class TokenType {
    /** Variable name token (e.g., "location", "tagName") */
    VARIABLE_NAME { override fun next() = OPERATOR },

    /** Operator token (e.g., "=", ">", "<") */
    OPERATOR { override fun next() = VALUE },

    /** Value token (e.g., "/path/to/file", 10) */
    VALUE { override fun next() = CONJUNCTION },

    /** Conjunction token (e.g., "AND", "OR") */
    CONJUNCTION { override fun next() = VARIABLE_NAME };

    /**
     * Returns the next token type in the sequence.
     * @return The next token type
     */
    abstract fun next(): TokenType

    companion object {
        /**
         * Returns the first token type in the sequence.
         * @return VARIABLE_NAME as the starting token type
         */
        fun first() = VARIABLE_NAME
    }
}

/**
 * Represents the type of entity in a DSL query.
 */
enum class EntityType {
    /** Represents a file entity */
    File,

    /** Represents a relationship between files */
    Relationship
}
