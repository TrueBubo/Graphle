package com.graphle.graphlemanager.file

import jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders.CONTENT_DISPOSITION
import org.springframework.http.HttpHeaders.CONTENT_LENGTH
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.web.bind.annotation.RequestParam
import java.io.IOException
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

typealias MimeContentType = String

@RestController
class FileDownloadController {
    @GetMapping("/download")
    fun serveFile(@RequestParam("path") path: String): ResponseEntity<Resource> {
        val filePath = Paths.get(path)
        val resource: Resource = FileSystemResource(filePath)

        if (!resource.exists()) return ResponseEntity.notFound().build()

        if (!resource.isReadable) return ResponseEntity.status(SC_FORBIDDEN).body(null)

        val contentType = filePath.mimeContentType()
        val fileName = filePath.fileName.toString()

        return try {
            ResponseEntity.ok()
                .header(CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
                .header(CONTENT_TYPE, contentType)
                .header(CONTENT_LENGTH, Files.size(filePath).toString())
                .body(resource)
        } catch (_: IOException) {
            ResponseEntity.internalServerError().body(null)
        }
    }

    private fun Path.mimeContentType(): MimeContentType {
        return URLConnection.guessContentTypeFromName(fileName.toString()) // From filename
            ?: Files.probeContentType(this) // From file header
            ?: "application/octet-stream" // Fallback to binary
    }
}