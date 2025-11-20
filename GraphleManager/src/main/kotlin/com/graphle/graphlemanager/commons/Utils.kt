package com.graphle.graphlemanager.commons

import com.graphle.graphlemanager.file.AbsolutePathString

/**
 * Normalizes a file path by removing trailing separators.
 * The root separator is never removed.
 * @return The normalized path string
 */
fun AbsolutePathString.normalize() =
    if (this == java.io.File.separator)
        this
    else
        if (this.endsWith(java.io.File.separator))
            this.dropLast(java.io.File.separator.length)
        else this