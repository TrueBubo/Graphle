package com.graphle.graphlemanager.dsl

import com.graphle.graphlemanager.BaseRestIntegrationTest
import com.graphle.graphlemanager.FileTestUtils
import com.graphle.graphlemanager.connection.ConnectionInput
import com.graphle.graphlemanager.connection.ConnectionService
import com.graphle.graphlemanager.tag.TagInput
import com.graphle.graphlemanager.tag.TagService
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.first
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import kotlin.test.Test

class DSLInterpreterIntegrationTest : BaseRestIntegrationTest() {
    @Autowired
    private lateinit var connectionService: ConnectionService
    @Autowired
    private lateinit var tagService: TagService
    @Autowired
    private lateinit var fileTestUtils: FileTestUtils
    private val randomString = (1..20).map { ('a'..'z').random() }.joinToString("")

    @Test
    fun `should run the select command via api`() = fileTestUtils.withTempFiles(3) { (file1, file2, file3) ->
        connectionService.addConnection(
            ConnectionInput(
                name = randomString,
                value = null,
                from = file1.absolutePath,
                to = file2.absolutePath,
                bidirectional = false
            )
        )

        connectionService.addConnection(
            ConnectionInput(
                name = randomString + "2",
                value = null,
                from = file2.absolutePath,
                to = file3.absolutePath,
                bidirectional = false
            )
        )

        expectThat(post<DSLResponse>(url = URL) {
            """
                { "command": "find (location = \"$file1\")[name = \"$randomString\"]()[name = \"${randomString}2\"]()[pred]()" }
            """.trimIndent()
        }).isNotNull().and {
            get { type }.isEqualTo(ResponseType.FILENAMES)
            get { responseObject }.hasSize(1).first().isEqualTo((file3.parent))
        }
    }

    @Test
    fun `should add relationship between files via api`() = fileTestUtils.withTempFiles(count = 2) { (file1, file2) ->
        expectThat(post<DSLResponse>(url = URL) {
            """
                { "command": "addRel \"${file1.absolutePath}\" \"${file2.absolutePath}\" \"$randomString\"" }
            """.trimIndent()
        }).isNotNull().and {
            get { type }.isEqualTo(ResponseType.SUCCESS)
        }

        // Verify via find query
        expectThat(post<DSLResponse>(url = URL) {
            """
                { "command": "find (location = \"${file1.absolutePath}\")[name = \"$randomString\"]" }
            """.trimIndent()
        }).isNotNull().and {
            get { type }.isEqualTo(ResponseType.CONNECTIONS)
            get { responseObject }.hasSize(1)
        }
    }

    @Test
    fun `should add relationship with value between files via api`() = fileTestUtils.withTempFiles(count = 2) { (file1, file2) ->
        expectThat(post<DSLResponse>(url = URL) {
            """
                { "command": "addRel \"${file1.absolutePath}\" \"${file2.absolutePath}\" \"$randomString\" \"testValue\"" }
            """.trimIndent()
        }).isNotNull().and {
            get { type }.isEqualTo(ResponseType.SUCCESS)
        }

        expectThat(post<DSLResponse>(url = URL) {
            """
                { "command": "find (location = \"${file1.absolutePath}\")[name = \"$randomString\"]" }
            """.trimIndent()
        }).isNotNull().and {
            get { type }.isEqualTo(ResponseType.CONNECTIONS)
            get { responseObject }.hasSize(1)
        }
    }

    @Test
    fun `should remove relationship between files via api`() = fileTestUtils.withTempFiles(count = 2) { (file1, file2) ->
        connectionService.addConnection(
            ConnectionInput(
                name = randomString,
                value = null,
                from = file1.absolutePath,
                to = file2.absolutePath,
                bidirectional = false
            )
        )

        expectThat(post<DSLResponse>(url = URL) {
            """
                { "command": "removeRel \"${file1.absolutePath}\" \"${file2.absolutePath}\" \"$randomString\"" }
            """.trimIndent()
        }).isNotNull().and {
            get { type }.isEqualTo(ResponseType.SUCCESS)
        }
    }

    @Test
    fun `should add tag to file via api`() = fileTestUtils.withTempFiles { (file) ->
        expectThat(post<DSLResponse>(url = URL) {
            """
                { "command": "addTag \"${file.absolutePath}\" \"$randomString\"" }
            """.trimIndent()
        }).isNotNull().and {
            get { type }.isEqualTo(ResponseType.SUCCESS)
        }

        val tags = tagService.tagsByFileLocation(file.absolutePath)
        expectThat(tags).hasSize(1).first().and {
            get { name }.isEqualTo(randomString)
        }
    }

    @Test
    fun `should add tag with value to file via api`() = fileTestUtils.withTempFiles { (file) ->
        expectThat(post<DSLResponse>(url = URL) {
            """
                { "command": "addTag \"${file.absolutePath}\" \"$randomString\" \"testValue\"" }
            """.trimIndent()
        }).isNotNull().and {
            get { type }.isEqualTo(ResponseType.SUCCESS)
        }

        val tags = tagService.tagsByFileLocation(file.absolutePath)
        expectThat(tags).hasSize(1).first().and {
            get { name }.isEqualTo(randomString)
            get { value }.isEqualTo("testValue")
        }
    }

    @Test
    fun `should remove tag from file via api`() = fileTestUtils.withTempFiles { (file) ->
        tagService.addTagToFile(
            location = file.absolutePath,
            tag = TagInput(name = randomString, value = null)
        )

        expectThat(post<DSLResponse>(url = URL) {
            """
                { "command": "removeTag \"${file.absolutePath}\" \"$randomString\"" }
            """.trimIndent()
        }).isNotNull().and {
            get { type }.isEqualTo(ResponseType.SUCCESS)
        }

        val tags = tagService.tagsByFileLocation(file.absolutePath)
        expectThat(tags).hasSize(0)
    }

    @Test
    fun `should remove tag with value from file via api`() = fileTestUtils.withTempFiles { (file) ->
        tagService.addTagToFile(
            location = file.absolutePath,
            tag = TagInput(name = randomString, value = "testValue")
        )

        expectThat(post<DSLResponse>(url = URL) {
            """
                { "command": "removeTag \"${file.absolutePath}\" \"$randomString\" \"testValue\"" }
            """.trimIndent()
        }).isNotNull().and {
            get { type }.isEqualTo(ResponseType.SUCCESS)
        }

        val tags = tagService.tagsByFileLocation(file.absolutePath)
        expectThat(tags).hasSize(0)
    }

    @Test
    fun `should get file details via api`() = fileTestUtils.withTempFiles { (file) ->
        expectThat(post<DSLResponse>(url = URL) {
            """
                { "command": "detail \"${file.absolutePath}\"" }
            """.trimIndent()
        }).isNotNull().and {
            get { type }.isEqualTo(ResponseType.FILE)
            get { responseObject }.hasSize(1)
        }
    }

    @Test
    fun `should return error for non-existent file details via api`() {
        val nonExistentPath = "/non/existent/path"

        expectThat(post<DSLResponse>(url = URL) {
            """
                { "command": "detail \"$nonExistentPath\"" }
            """.trimIndent()
        }).isNotNull().and {
            get { type }.isEqualTo(ResponseType.ERROR)
            get { responseObject }.hasSize(1).first().contains("not found")
        }
    }

    @Test
    fun `should get files by tag name via api`() = fileTestUtils.withTempFiles(count = 2) { (file1, file2) ->
        // Add same tag to both files
        tagService.addTagToFile(
            location = file1.absolutePath,
            tag = TagInput(name = randomString, value = null)
        )
        tagService.addTagToFile(
            location = file2.absolutePath,
            tag = TagInput(name = randomString, value = null)
        )

        expectThat(post<DSLResponse>(url = URL) {
            """
                { "command": "tag \"$randomString\"" }
            """.trimIndent()
        }).isNotNull().and {
            get { type }.isEqualTo(ResponseType.TAG)
            get { responseObject }.hasSize(2)
        }
    }

    @Test
    fun `should return empty list for tag with no files via api`() {
        expectThat(post<DSLResponse>(url = URL) {
            """
                { "command": "tag \"nonExistentTag\"" }
            """.trimIndent()
        }).isNotNull().and {
            get { type }.isEqualTo(ResponseType.TAG)
            get { responseObject }.hasSize(0)
        }
    }

    companion object {
        const val URL = "/dsl"
    }
}