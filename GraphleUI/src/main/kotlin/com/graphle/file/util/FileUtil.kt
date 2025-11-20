package com.graphle.file.util

import com.graphle.common.downloadURL
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import java.awt.Desktop
import java.io.File

/**
 * Creates parent directories for a file path if they don't exist.
 *
 * @param filePath Absolute path to the file
 * @return File object for the given path
 */
fun createParentDirectories(filePath: String): File {
    val file = File(filePath)

    file.parentFile?.takeIf { !it.exists() }?.mkdirs()

    return file
}

/**
 * Opens a file using the system's default application.
 *
 * @param file File to open
 */
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

/**
 * Downloads a file from the server to a local destination.
 *
 * @param filePath Server path of the file to download
 * @param destinationFile Local file destination
 * @return true if download succeeded, false otherwise
 */
suspend fun downloadFile(filePath: String, destinationFile: File): Boolean {
    val client = HttpClient(CIO)
    try {
        val response = client.get("${downloadURL}?path=$filePath")
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