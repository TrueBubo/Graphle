package com.graphle.graphlemanager.dsl

/**
 * Utility object providing DSL parsing and string manipulation functions.
 */
object DSLUtil {
    /**
     * Splits a DSL command string into individual tokens.
     * Handles quoted strings, parentheses, and escape sequences properly.
     * @param text The DSL command string to tokenize
     * @return A list of tokens extracted from the input string
     */
    fun splitIntoTokens(text: String): List<String>  = buildList {
        var inQuotes = false
        var isEscaped = false
        val word = StringBuilder()
        for (char in text) {
            if ((char == '(' || char == ')') && !inQuotes) {
                if (!word.isEmpty()) add(word.toString())
                word.clear()
                add(char.toString())
                continue
            }
            if (char == '"' && !isEscaped) {
                inQuotes = !inQuotes
                word.append(char)
                if (!inQuotes) {
                    add(word.toString())
                    word.clear()
                }
                continue

            }
            val wasEscaped = isEscaped
            isEscaped = false
            if (char == '\\' && wasEscaped) {
                word.append('\\')
                continue
            } else if (char == '\\'){
                isEscaped = true
                continue
            }

            if (!inQuotes && char == ' ' && word.isEmpty()) {
                continue
            } else if (!inQuotes && char == ' ') {
                add(word.toString())
                word.clear()
                continue
            }

            word.append(char)
        }
        if (word.isNotEmpty()) add(word.toString())
    }

    /**
     * Removes surrounding quotes from a string if present.
     * @return The string without quotes if it was quoted, otherwise the original string
     */
    fun String.removeQuotes() = if (this.first() == '"' && this.last() == '"') this.substring(1, length - 1) else this

    /**
     * Ensures a string is properly quoted.
     * Removes existing quotes first, then adds new quotes around the string.
     * @return The quoted string
     */
    fun String.ensureQuoted() = this.removeQuotes().let { "\"$it\"" }
}