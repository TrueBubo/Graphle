package com.graphle.graphlemanager.file

import com.graphle.graphlemanager.BaseRestIntegrationTest
import com.graphle.graphlemanager.FileTestUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders.CONTENT_DISPOSITION
import org.springframework.http.HttpHeaders.CONTENT_LENGTH
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.nio.file.Files
import kotlin.test.Test

class FileDownloadControllerTest : BaseRestIntegrationTest() {
    @Autowired
    private lateinit var fileTestUtils: FileTestUtils

    @Test
    fun `should download existing file with correct headers`() = fileTestUtils.withTempFiles { (file) ->
        val content = "Hello, World!"
        Files.writeString(file.toPath(), content)

        val result = mockMvc.perform(
            get("/download")
                .param("path", file.absolutePath)
        )
            .andExpect(status().isOk)
            .andExpect(header().exists(CONTENT_DISPOSITION))
            .andExpect(header().exists(CONTENT_TYPE))
            .andExpect(header().exists(CONTENT_LENGTH))
            .andReturn()

        val contentDisposition = result.response.getHeader(CONTENT_DISPOSITION)
        expectThat(contentDisposition).isEqualTo("attachment; filename=\"${file.name}\"")

        val contentLength = result.response.getHeader(CONTENT_LENGTH)
        expectThat(contentLength).isEqualTo(Files.size(file.toPath()).toString())

        val responseContent = result.response.contentAsString
        expectThat(responseContent).isEqualTo(content)
    }

    @Test
    fun `should return 404 for non-existent file`() {
        val nonExistentPath = "/non/existent/path/file.txt"

        mockMvc.perform(
            get("/download")
                .param("path", nonExistentPath)
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should detect MIME type for text files`() = fileTestUtils.withTempFiles { (file) ->
        val txtFile = file.resolveSibling("${file.name}.txt")
        Files.writeString(txtFile.toPath(), "text content")

        try {
            val result = mockMvc.perform(
                get("/download")
                    .param("path", txtFile.absolutePath)
            )
                .andExpect(status().isOk)
                .andReturn()

            val contentType = result.response.getHeader(CONTENT_TYPE)
            expectThat(contentType).isEqualTo("text/plain")
        } finally {
            Files.deleteIfExists(txtFile.toPath())
        }
    }

    @Test
    fun `should detect MIME type for JSON files`() = fileTestUtils.withTempFiles { (file) ->
        val jsonFile = file.resolveSibling("${file.name}.json")
        Files.writeString(jsonFile.toPath(), """{"key": "value"}""")

        try {
            val result = mockMvc.perform(
                get("/download")
                    .param("path", jsonFile.absolutePath)
            )
                .andExpect(status().isOk)
                .andReturn()

            val contentType = result.response.getHeader(CONTENT_TYPE)
            expectThat(contentType).isEqualTo("application/json")
        } finally {
            Files.deleteIfExists(jsonFile.toPath())
        }
    }

    @Test
    fun `should fallback to octet-stream for unknown file types`() = fileTestUtils.withTempFiles { (file) ->
        val unknownFile = file.resolveSibling("${file.name}.unknown")
        Files.writeString(unknownFile.toPath(), "binary content")

        try {
            val result = mockMvc.perform(
                get("/download")
                    .param("path", unknownFile.absolutePath)
            )
                .andExpect(status().isOk)
                .andReturn()

            val contentType = result.response.getHeader(CONTENT_TYPE)
            expectThat(contentType).isEqualTo("application/octet-stream")
        } finally {
            Files.deleteIfExists(unknownFile.toPath())
        }
    }

    @Test
    fun `should download binary files correctly`() = fileTestUtils.withTempFiles { (file) ->
        val binaryContent = byteArrayOf(0x00, 0x01, 0x02, 0x03, 0xFF.toByte())
        Files.write(file.toPath(), binaryContent)

        val result = mockMvc.perform(
            get("/download")
                .param("path", file.absolutePath)
        )
            .andExpect(status().isOk)
            .andReturn()

        val responseBytes = result.response.contentAsByteArray
        expectThat(responseBytes).isEqualTo(binaryContent)
    }

    @Test
    fun `should handle empty files`() = fileTestUtils.withTempFiles { (file) ->
        Files.writeString(file.toPath(), "")

        val result = mockMvc.perform(
            get("/download")
                .param("path", file.absolutePath)
        )
            .andExpect(status().isOk)
            .andReturn()

        val contentLength = result.response.getHeader(CONTENT_LENGTH)
        expectThat(contentLength).isEqualTo("0")

        val responseContent = result.response.contentAsString
        expectThat(responseContent).isEqualTo("")
    }

    @Test
    fun `should handle files with special characters in name`() = fileTestUtils.withTempFiles { (file) ->
        val specialFile = file.resolveSibling("test file (1) [copy].txt")
        Files.writeString(specialFile.toPath(), "content")

        try {
            val result = mockMvc.perform(
                get("/download")
                    .param("path", specialFile.absolutePath)
            )
                .andExpect(status().isOk)
                .andReturn()

            val contentDisposition = result.response.getHeader(CONTENT_DISPOSITION)
            expectThat(contentDisposition).isEqualTo("attachment; filename=\"test file (1) [copy].txt\"")
        } finally {
            Files.deleteIfExists(specialFile.toPath())
        }
    }

    @Test
    fun `should detect MIME type for HTML files`() = fileTestUtils.withTempFiles { (file) ->
        val htmlFile = file.resolveSibling("${file.name}.html")
        Files.writeString(htmlFile.toPath(), "<html><body>Test</body></html>")

        try {
            val result = mockMvc.perform(
                get("/download")
                    .param("path", htmlFile.absolutePath)
            )
                .andExpect(status().isOk)
                .andReturn()

            val contentType = result.response.getHeader(CONTENT_TYPE)
            expectThat(contentType).isEqualTo("text/html")
        } finally {
            Files.deleteIfExists(htmlFile.toPath())
        }
    }

    @Test
    fun `should detect MIME type for XML files`() = fileTestUtils.withTempFiles { (file) ->
        val xmlFile = file.resolveSibling("${file.name}.xml")
        Files.writeString(xmlFile.toPath(), "<?xml version=\"1.0\"?><root></root>")

        try {
            val result = mockMvc.perform(
                get("/download")
                    .param("path", xmlFile.absolutePath)
            )
                .andExpect(status().isOk)
                .andReturn()

            val contentType = result.response.getHeader(CONTENT_TYPE)
            expectThat(contentType).isEqualTo("application/xml")
        } finally {
            Files.deleteIfExists(xmlFile.toPath())
        }
    }
}