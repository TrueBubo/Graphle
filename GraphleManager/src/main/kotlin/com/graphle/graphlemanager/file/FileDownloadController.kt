package com.graphle.graphlemanager.file

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.RequestParam
import java.io.IOException
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Paths

@RestController
class FileDownloadController {
    @GetMapping("/download")
    fun serveFile(@RequestParam("path") path: String): ResponseEntity<Resource> {
        val filePath = Paths.get(path)
        val resource: Resource = FileSystemResource(filePath)

        if (!resource.exists()) return ResponseEntity.notFound().build()

        if (!resource.isReadable) return ResponseEntity.status(403).body(null)

        val contentType = guessContentType(filePath.toString())
        val fileName = filePath.fileName.toString()

        return try {
            ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_LENGTH, Files.size(filePath).toString())
                .body(resource)
        } catch (_: IOException) {
            ResponseEntity.status(500).body(null)
        }
    }

    private fun guessContentType(filePath: String): String {
        return URLConnection.guessContentTypeFromName(filePath) // From filename
            ?: Files.probeContentType(Paths.get(filePath)) // From file header
            ?: "application/octet-stream" // Fallback to binary
    }
}