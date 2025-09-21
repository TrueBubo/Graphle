package com.graphle

import java.awt.Desktop
import java.io.File

fun openFile(file: File) {
    if (!file.exists()) {
        return
    }

    if (Desktop.isDesktopSupported()) {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.OPEN)) {
            desktop.open(file)
        } else {
            println("OPEN action is not supported on this platform")
        }
    } else {
        println("Desktop API is not supported")
    }
}