package com.graphle.graphlemanager.dsl

object DSLUtil {
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

    fun String.removeQuotes() = if (this.first() == '"' && this.last() == '"') this.drop(1).dropLast(1) else this

    fun String.ensureQuoted() = this.removeQuotes().let { "\"$it\"" }
}