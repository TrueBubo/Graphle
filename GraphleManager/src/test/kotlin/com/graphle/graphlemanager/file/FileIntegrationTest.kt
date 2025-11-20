package com.graphle.graphlemanager.file

import com.graphle.graphlemanager.BaseGraphQlIntegrationTest
import com.graphle.graphlemanager.FileTestUtils
import com.graphle.graphlemanager.randomString
import com.graphle.graphlemanager.sweeper.Neo4JSweeper
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.first
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.test.AfterTest
import kotlin.test.Test

class FileIntegrationTest(
    @Autowired private val fileTestUtils: FileTestUtils,
    @Autowired private val neo4JSweeper: Neo4JSweeper
) : BaseGraphQlIntegrationTest() {
    private val randomString = randomString(20)

    @AfterTest
    fun tearDown() {
        neo4JSweeper.sweep()
    }

    @Test
    fun `add file`() = fileTestUtils.withTempFiles { (file) ->
        post { insertFileQuery(location = file.absolutePath) }
    }

    @Test
    fun `file was inserted and fetched`() = fileTestUtils.withTempFiles { (file) ->
        post { insertFileQuery(location = file.absolutePath) }
        expectThat(post<File> { fileByLocationQuery(location = file.absolutePath) })
            .isNotNull()
            .and {
                get { location }.isEqualTo(file.absolutePath)
                get { tags }.isEmpty()
                get { connections }.hasSize(1).first().get { name }.isEqualTo("parent")
            }
    }

    @Test
    fun `non-existent file returns null`() {
        expectThat(post<File?> { fileByLocationQuery(location = "/this/file/does/not/exist/$randomString") }).isEqualTo(
            null
        )
    }

    @Test
    fun `remove file`() = fileTestUtils.withTempFiles { (file) ->
        post { insertFileQuery(location = file.absolutePath) }

        expectThat(post<File?> { fileByLocationQuery(location = file.absolutePath) }).isNotNull()

        post { removeFileQuery(location = file.absolutePath) }

        expectThat(post<File?> { fileByLocationQuery(location = file.absolutePath) }).isNull()
    }

    @Test
    fun `move file`() = fileTestUtils.withTempFiles(count = 1) { (old) ->
        post { insertFileQuery(location = old.absolutePath) }
        val newDir = Files.createTempDirectory("tmp")
        val newFilePathString = Path(newDir.absolutePathString(), "tmp").absolutePathString()

        try {
            expectThat(post<File?> { fileByLocationQuery(location = old.absolutePath) }).isNotNull()

            post {
                moveFileQuery(
                    from = old.absolutePath,
                    to = newFilePathString
                )
            }
            expectThat(post<File?> { fileByLocationQuery(location = old.absolutePath) }).isNull()
            expectThat(post<File?> {
                fileByLocationQuery(
                    location = newFilePathString,
                )
            }).isNotNull()
                .isNotNull()
                .and {
                    get { location }.isEqualTo(newFilePathString)
                    get { tags }.isEmpty()
                    get { connections }.hasSize(1).first().get { name }.isEqualTo("parent")
                }
        } finally {
            FileUtils.deleteQuietly(newDir.toFile())
        }
    }

    private fun insertFileQuery(location: AbsolutePathString) = mutation {
        addFile(location)
    }

    private fun fileByLocationQuery(location: AbsolutePathString) = query {
        fileByLocation(location)
    }

    private fun removeFileQuery(location: AbsolutePathString) = mutation {
        removeFile(location)
    }

    private fun moveFileQuery(from: AbsolutePathString, to: AbsolutePathString) = mutation {
        moveFile(from, to)
    }

    companion object {
        private fun addFile(location: AbsolutePathString) =
            """
            addFile(location: "$location") {
                location
            }
            """.trimIndent()

        private fun fileByLocation(location: AbsolutePathString) =
            """
            fileByLocation(location: "$location") {
                location
                tags {
                    name
                    value
                }
                
                connections {
                    name
                    value
                    from
                    to
                }
            }
            """.trimIndent()

        private fun removeFile(location: AbsolutePathString) =
            """
            removeFile(location: "$location")
            """.trimIndent()

        private fun moveFile(from: AbsolutePathString, to: AbsolutePathString) =
            """
            moveFile(locationFrom: "$from", locationTo: "$to") {
                from
                to
            }
            """.trimIndent()
    }
}