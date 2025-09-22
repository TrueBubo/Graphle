package com.graphle

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
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

suspend fun downloadFile(filePath: String, destinationFile: File): Boolean {
    val client = HttpClient(CIO)
    try {
        val response = client.get("$downloadURL?path=$filePath")
        if (response.status.value == 200) {
            response.bodyAsChannel().toInputStream().use { inputStream ->
                destinationFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            println("File downloaded successfully")
            return true
        } else {
            println("Failed to download file. HTTP status: ${response.status}")
            return false
        }
    } catch (e: Exception) {
        println("Error downloading file: ${e.message}")
        return false
    } finally {
        client.close()
    }
}