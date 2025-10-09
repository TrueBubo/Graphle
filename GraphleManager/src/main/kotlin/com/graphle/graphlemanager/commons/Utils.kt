package com.graphle.graphlemanager.commons

import com.graphle.graphlemanager.file.AbsolutePathString

fun AbsolutePathString.normalize() =
    if (this.endsWith(java.io.File.separator))
        this.dropLast(java.io.File.separator.length)
    else this