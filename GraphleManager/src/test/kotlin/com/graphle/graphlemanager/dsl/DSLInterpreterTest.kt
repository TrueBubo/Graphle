package com.graphle.graphlemanager.dsl

import com.graphle.graphlemanager.FileTestUtils
import com.graphle.graphlemanager.connection.Connection
import com.graphle.graphlemanager.connection.ConnectionInput
import com.graphle.graphlemanager.connection.ConnectionService
import com.graphle.graphlemanager.randomString
import com.graphle.graphlemanager.sweeper.Neo4JSweeper
import com.graphle.graphlemanager.tag.TagForFile
import com.graphle.graphlemanager.tag.TagInput
import com.graphle.graphlemanager.tag.TagService
import com.graphle.graphlemanager.file.File as GraphleFile
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.containsExactly
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.first
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import kotlin.test.AfterTest
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DSLInterpreterTest {
    private val randomString = randomString(20)

    @Autowired
    private lateinit var neo4JSweeper: Neo4JSweeper

    @Autowired
    private lateinit var connectionService: ConnectionService

    @Autowired
    private lateinit var tagService: TagService

    @Autowired
    private lateinit var fileTestUtils: FileTestUtils

    @Autowired
    private lateinit var interpreter: DSLInterpreter

    @Autowired
    private lateinit var dslCommandExecutor: DSLCommandExecutor

    @Autowired
    private lateinit var dslScopeParser: DSLScopeParser

    @Autowired
    private lateinit var cypherQueryBuilder: CypherQueryBuilder

    @AfterTest
    fun tearDown() {
        neo4JSweeper.sweep()
    }

    @Test
    fun `should execute using the whole command for hierarchical connection`() =
        fileTestUtils.withTempFiles(count = 2) { (file1, file2) ->
            connectionService.addConnection(
                ConnectionInput(
                    name = randomString,
                    value = null,
                    from = file1.absolutePath,
                    to = file2.absolutePath,
                    bidirectional = false
                )
            )

            val response = interpreter.interpret("find (location = \"${file1.parent}\")[desc]")
            expectThat(response).and {
                get { type }.isEqualTo(ResponseType.CONNECTIONS)
                get {
                    responseObject
                        .map { Json.decodeFromString<Connection>(it) }
                        .map { it.to }
                }.contains(file1.absolutePath, file2.absolutePath)
            }
        }

    @Test
    fun `should execute using the whole command for custom connection`() =
        fileTestUtils.withTempFiles(count = 2) { (file1, file2) ->
            connectionService.addConnection(
                ConnectionInput(
                    name = randomString,
                    value = null,
                    from = file1.absolutePath,
                    to = file2.absolutePath,
                    bidirectional = false
                )
            )

            val response = interpreter.interpret("find (location = \"$file1\")[name = \"$randomString\"]")
            expectThat(response).and {
                get { type }.isEqualTo(ResponseType.CONNECTIONS)
                get {
                    responseObject
                        .map { Json.decodeFromString<Connection>(it) }
                        .map { it.to }
                }.hasSize(1).first().isEqualTo(file2.absolutePath)
            }
        }

    @Test
    fun `should handle longer chain`() {
        fileTestUtils.withTempFiles(count = 3) { (file1, file2, file3) ->
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

            val response = interpreter.interpret(
                "find (location = \"$file1\")[name = \"$randomString\"]()[name = \"${randomString}2\"]()[pred]()"
            )
            expectThat(response).and {
                get { type }.isEqualTo(ResponseType.FILENAMES)
                get { responseObject }.hasSize(1).first().isEqualTo((file3.parent))
            }
        }
    }

    @Test
    fun `invalid file syntax returns error response`() {
        val response = interpreter.interpret("find (location = \" or tagName = \"${randomString}\")")

        expectThat(response).and {
            get { type }.isEqualTo(ResponseType.ERROR)
            get { responseObject }.hasSize(1).first().contains("Unable to parse")
        }
    }

    @Test
    fun `invalid connection syntax returns error response`() {
        val response = interpreter.interpret("find ()[nonExistent = 2]")
        expectThat(response).and {
            get { type }.isEqualTo(ResponseType.ERROR)
            get { responseObject }.hasSize(1).first().contains("Failed to build query for scope: nonExistent = 2")
        }
    }

    @Test
    fun `should return file matching the file scope command`() =
        fileTestUtils.withTempFiles(count = 2) { (file1, file2) ->
            connectionService.addConnection(
                ConnectionInput(
                    name = "",
                    value = null,
                    from = file1.absolutePath,
                    to = file2.absolutePath,
                    bidirectional = false
                )
            )
            tagService.addTagToFile(
                location = file2.absolutePath,
                tag = TagInput(name = randomString, value = randomString)
            )
            val response = dslCommandExecutor.executeFindScope(
                scope = Scope(
                    entityType = EntityType.File,
                    text = "location = \"${file1.absolutePath}\" or tagName = \"${randomString}\"",
                ),
                prevSelectedFilenames = null
            )

            expectThat(response.type).isEqualTo(ResponseType.FILENAMES)
            expectThat(response.responseObject).hasSize(2)
                .and { containsExactlyInAnyOrder(file1.absolutePath, file2.absolutePath) }
        }


    @Test
    fun `should return file with tag matching the range`() = fileTestUtils.withTempFiles(count = 2) { (file1, file2) ->
        tagService.addTagToFile(
            location = file1.absolutePath,
            tag = TagInput(name = randomString, value = "8")
        )
        tagService.addTagToFile(
            location = file2.absolutePath,
            tag = TagInput(name = randomString, value = "11.2")
        )
        val response = dslCommandExecutor.executeFindScope(
            scope = Scope(
                entityType = EntityType.File,
                text = "location = \"${file1.absolutePath}\" or location = \"${file2.absolutePath}\" AND " +
                        "tagName = \"${randomString}\" AND (tagValue < 9 OR tagValue > 12)",
            ),
            prevSelectedFilenames = null
        )

        expectThat(response.type).isEqualTo(ResponseType.FILENAMES)
        expectThat(response.responseObject).hasSize(1)
            .and { containsExactly(file1.absolutePath) }
    }


    @Test
    fun `should return file connected via the relationship command`() =
        fileTestUtils.withTempFiles(count = 2) { (file1, file2) ->
            connectionService.addConnection(
                ConnectionInput(
                    name = randomString,
                    value = null,
                    from = file1.absolutePath,
                    to = file2.absolutePath,
                    bidirectional = false
                )
            )

            val response = dslCommandExecutor.executeFindScope(
                scope = Scope(
                    entityType = EntityType.Relationship,
                    text = "name = \"${randomString}\""
                ),
                prevSelectedFilenames = listOf(file1.absolutePath),
            )

            expectThat(response.type).isEqualTo(ResponseType.CONNECTIONS)
            expectThat(response.responseObject.map { Json.decodeFromString<Connection>(it) })
                .hasSize(1).first().and {
                    get { name }.isEqualTo(randomString)
                    get { from }.isEqualTo(file1.absolutePath)
                    get { to }.isEqualTo(file2.absolutePath)
                }
        }

    @Test
    fun `should return tree edges`() = fileTestUtils.withTempFiles { (file) ->
        val response = dslCommandExecutor.executeFindScope(
            scope = Scope(
                entityType = EntityType.Relationship,
                text = "PRED",
            ),
            prevSelectedFilenames = listOf(file.absolutePath),
        )
        expectThat(response.type).isEqualTo(ResponseType.CONNECTIONS)
        expectThat(response.responseObject.map { Json.decodeFromString<Connection>(it) })
            .hasSize(1)
            .first()
            .isEqualTo(
                Connection(
                    name = "parent",
                    from = file.absolutePath,
                    to = file.parent,
                )
            )
    }

    @Test
    fun `dsl correctly splits into tokens`() {
        val scope = """tagName = "name" AND (location = "/home/(Person)") OR tagValue > 3"""
        val tokens = DSLUtil.splitIntoTokens(scope)
        expectThat(tokens).isEqualTo(
            listOf(
                "tagName",
                "=",
                "\"name\"",
                "AND",
                "(",
                "location",
                "=",
                "\"/home/(Person)\"",
                ")",
                "OR",
                "tagValue",
                ">",
                "3"
            )
        )
    }

    @Test
    fun `parser splits search command to scopes`() {
        val command = """(tagName = "name")[name = "friend"](location = "/home/(Person)")"""
        val scopes = dslScopeParser.splitSearchIntoScopes(command)
        expectThat(scopes).hasSize(3).containsExactly(
            Scope(entityType = EntityType.File, text = """tagName = "name""""),
            Scope(entityType = EntityType.Relationship, text = """name = "friend""""),
            Scope(entityType = EntityType.File, text = """location = "/home/(Person)"""")
        )
    }

    @Test
    fun `interpreter splits search command with priorities`() {
        val command = """(tagName = "name" AND (location = "/home"))[name = "friend"](location = "/home/(Person)")"""
        val scopes = dslScopeParser.splitSearchIntoScopes(command)
        expectThat(scopes).hasSize(3).containsExactly(
            Scope(entityType = EntityType.File, text = """tagName = "name" AND (location = "/home")"""),
            Scope(entityType = EntityType.Relationship, text = """name = "friend""""),
            Scope(entityType = EntityType.File, text = """location = "/home/(Person)"""")
        )
    }

    @Test
    fun `interpreter correctly translates file scope into neo4j`() {
        val scope = Scope(
            entityType = EntityType.File,
            text = """tagName = "year" AND tagValue > 2015 AND (location != "/home/(Person)")"""
        )
    expectThat(cypherQueryBuilder.convertScopeToQuery(scope = scope, prevSelectedFilename = null))
            .isEqualTo(
                "MATCH (f:File) OPTIONAL MATCH (f)-[:HasTag]-(t:Tag) " +
                        "WITH f, t WHERE t.name = \"year\" AND toFloat(t.value) > 2015 AND ( f.location <> \"/home/(Person)\" ) RETURN f"
            )
    }

    @Test
    fun `should add relationship between files`() = fileTestUtils.withTempFiles(count = 2) { (file1, file2) ->
        val response = interpreter.interpret("addRel \"${file1.absolutePath}\" \"${file2.absolutePath}\" \"$randomString\"")

        expectThat(response).and {
            get { type }.isEqualTo(ResponseType.SUCCESS)
        }

        val verifyResponse = interpreter.interpret("find (location = \"${file1.absolutePath}\")[name = \"$randomString\"]")
        expectThat(verifyResponse.type).isEqualTo(ResponseType.CONNECTIONS)
    }

    @Test
    fun `should add relationship with value between files`() = fileTestUtils.withTempFiles(count = 2) { (file1, file2) ->
        val response = interpreter.interpret("addRel \"${file1.absolutePath}\" \"${file2.absolutePath}\" \"$randomString\" \"testValue\"")

        expectThat(response).and {
            get { type }.isEqualTo(ResponseType.SUCCESS)
        }

        val verifyResponse = interpreter.interpret("find (location = \"${file1.absolutePath}\")[name = \"$randomString\"]")
        expectThat(verifyResponse.type).isEqualTo(ResponseType.CONNECTIONS)
    }

    @Test
    fun `should remove relationship between files`() = fileTestUtils.withTempFiles(count = 2) { (file1, file2) ->
        connectionService.addConnection(
            ConnectionInput(
                name = randomString,
                value = null,
                from = file1.absolutePath,
                to = file2.absolutePath,
                bidirectional = false
            )
        )

        val response = interpreter.interpret("removeRel \"${file1.absolutePath}\" \"${file2.absolutePath}\" \"$randomString\"")

        expectThat(response).and {
            get { type }.isEqualTo(ResponseType.SUCCESS)
        }
    }

    @Test
    fun `should add tag to file`() = fileTestUtils.withTempFiles { (file) ->
        val response = interpreter.interpret("addTag \"${file.absolutePath}\" \"$randomString\"")

        expectThat(response).and {
            get { type }.isEqualTo(ResponseType.SUCCESS)
        }

        val tags = tagService.tagsByFileLocation(file.absolutePath)
        expectThat(tags).hasSize(1).first().and {
            get { name }.isEqualTo(randomString)
        }
    }

    @Test
    fun `should add tag with value to file`() = fileTestUtils.withTempFiles { (file) ->
        val response = interpreter.interpret("addTag \"${file.absolutePath}\" \"$randomString\" \"testValue\"")

        expectThat(response).and {
            get { type }.isEqualTo(ResponseType.SUCCESS)
        }

        val tags = tagService.tagsByFileLocation(file.absolutePath)
        expectThat(tags).hasSize(1).first().and {
            get { name }.isEqualTo(randomString)
            get { value }.isEqualTo("testValue")
        }
    }

    @Test
    fun `should remove tag from file`() = fileTestUtils.withTempFiles { (file) ->
        tagService.addTagToFile(
            location = file.absolutePath,
            tag = TagInput(name = randomString, value = null)
        )

        val response = interpreter.interpret("removeTag \"${file.absolutePath}\" \"$randomString\"")

        expectThat(response).and {
            get { type }.isEqualTo(ResponseType.SUCCESS)
        }

        val tags = tagService.tagsByFileLocation(file.absolutePath)
        expectThat(tags).hasSize(0)
    }

    @Test
    fun `should remove tag with value from file`() = fileTestUtils.withTempFiles { (file) ->
        tagService.addTagToFile(
            location = file.absolutePath,
            tag = TagInput(name = randomString, value = "testValue")
        )

        val response = interpreter.interpret("removeTag \"${file.absolutePath}\" \"$randomString\" \"testValue\"")

        expectThat(response).and {
            get { type }.isEqualTo(ResponseType.SUCCESS)
        }

        val tags = tagService.tagsByFileLocation(file.absolutePath)
        expectThat(tags).hasSize(0)
    }

    @Test
    fun `should get file details`() = fileTestUtils.withTempFiles { (file) ->
        val response = interpreter.interpret("detail \"${file.absolutePath}\"")

        expectThat(response).and {
            get { type }.isEqualTo(ResponseType.FILE)
            get { responseObject }.hasSize(1)
        }

        val fileDetail = Json.decodeFromString<GraphleFile>(response.responseObject.first())
        expectThat(fileDetail.location).isEqualTo(file.absolutePath)
    }

    @Test
    fun `should return error for non-existent file details`() {
        val nonExistentPath = "/non/existent/path"

        val response = interpreter.interpret("detail \"$nonExistentPath\"")

        expectThat(response).and {
            get { type }.isEqualTo(ResponseType.ERROR)
            get { responseObject }.hasSize(1).first().contains("not found")
        }
    }

    @Test
    fun `should get files by tag name`() = fileTestUtils.withTempFiles(count = 2) { (file1, file2) ->
        tagService.addTagToFile(
            location = file1.absolutePath,
            tag = TagInput(name = randomString, value = null)
        )
        tagService.addTagToFile(
            location = file2.absolutePath,
            tag = TagInput(name = randomString, value = null)
        )

        val response = interpreter.interpret("tag \"$randomString\"")

        expectThat(response).and {
            get { type }.isEqualTo(ResponseType.TAG)
            get { responseObject }.hasSize(2)
        }

        val tagLocations = response.responseObject.map { Json.decodeFromString<TagForFile>(it) }
        expectThat(tagLocations.map { it.location }).containsExactlyInAnyOrder(file1.absolutePath, file2.absolutePath)
    }

    @Test
    fun `should return empty list for tag with no files`() {
        val response = interpreter.interpret("tag \"nonExistentTag\"")

        expectThat(response).and {
            get { type }.isEqualTo(ResponseType.TAG)
            get { responseObject }.hasSize(0)
        }
    }

    @Test
    fun `should filter out non-numeric tag values in numeric comparisons`() = fileTestUtils.withTempFiles(count = 3) { (file1, file2, file3) ->
        tagService.addTagToFile(
            location = file1.absolutePath,
            tag = TagInput(name = randomString, value = "10")
        )

        tagService.addTagToFile(
            location = file2.absolutePath,
            tag = TagInput(name = randomString, value = "abc")
        )

        tagService.addTagToFile(
            location = file3.absolutePath,
            tag = TagInput(name = randomString, value = "5")
        )

        val response = dslCommandExecutor.executeFindScope(
            scope = Scope(
                entityType = EntityType.File,
                text = "tagName = \"$randomString\" AND tagValue > 8"
            ),
            prevSelectedFilenames = null
        )

        expectThat(response.type).isEqualTo(ResponseType.FILENAMES)
        expectThat(response.responseObject).hasSize(1).containsExactly(file1.absolutePath)
    }

    @Test
    fun `should handle numeric comparison with less than operator`() = fileTestUtils.withTempFiles(count = 3) { (file1, file2, file3) ->
        tagService.addTagToFile(
            location = file1.absolutePath,
            tag = TagInput(name = randomString, value = "3.5")
        )

        tagService.addTagToFile(
            location = file2.absolutePath,
            tag = TagInput(name = randomString, value = "xyz")
        )

        tagService.addTagToFile(
            location = file3.absolutePath,
            tag = TagInput(name = randomString, value = "10")
        )

        val response = dslCommandExecutor.executeFindScope(
            scope = Scope(
                entityType = EntityType.File,
                text = "tagName = \"$randomString\" AND tagValue < 5"
            ),
            prevSelectedFilenames = null
        )

        expectThat(response.type).isEqualTo(ResponseType.FILENAMES)
        expectThat(response.responseObject).hasSize(1).containsExactly(file1.absolutePath)
    }

    @Test
    fun `should handle numeric comparison with greater than or equal operator`() = fileTestUtils.withTempFiles(count = 4) { (file1, file2, file3, file4) ->
        tagService.addTagToFile(
            location = file1.absolutePath,
            tag = TagInput(name = randomString, value = "10")
        )

        tagService.addTagToFile(
            location = file2.absolutePath,
            tag = TagInput(name = randomString, value = "15")
        )

        tagService.addTagToFile(
            location = file3.absolutePath,
            tag = TagInput(name = randomString, value = "5")
        )

        tagService.addTagToFile(
            location = file4.absolutePath,
            tag = TagInput(name = randomString, value = "notanumber")
        )

        val response = dslCommandExecutor.executeFindScope(
            scope = Scope(
                entityType = EntityType.File,
                text = "tagName = \"$randomString\" AND tagValue >= 10"
            ),
            prevSelectedFilenames = null
        )

        expectThat(response.type).isEqualTo(ResponseType.FILENAMES)
        expectThat(response.responseObject).hasSize(2)
            .containsExactlyInAnyOrder(file1.absolutePath, file2.absolutePath)
    }

    @Test
    fun `should handle numeric comparison with less than or equal operator`() = fileTestUtils.withTempFiles(count = 3) { (file1, file2, file3) ->
        tagService.addTagToFile(
            location = file1.absolutePath,
            tag = TagInput(name = randomString, value = "7")
        )

        tagService.addTagToFile(
            location = file2.absolutePath,
            tag = TagInput(name = randomString, value = "7.0")
        )

        tagService.addTagToFile(
            location = file3.absolutePath,
            tag = TagInput(name = randomString, value = "8")
        )

        val response = dslCommandExecutor.executeFindScope(
            scope = Scope(
                entityType = EntityType.File,
                text = "tagName = \"$randomString\" AND tagValue <= 7"
            ),
            prevSelectedFilenames = null
        )

        expectThat(response.type).isEqualTo(ResponseType.FILENAMES)
        expectThat(response.responseObject).hasSize(2)
            .containsExactlyInAnyOrder(file1.absolutePath, file2.absolutePath)
    }

    @Test
    fun `should still use string comparison for equality operator`() = fileTestUtils.withTempFiles(count = 2) { (file1, file2) ->
        tagService.addTagToFile(
            location = file1.absolutePath,
            tag = TagInput(name = randomString, value = "test")
        )

        tagService.addTagToFile(
            location = file2.absolutePath,
            tag = TagInput(name = randomString, value = "10")
        )

        val response = dslCommandExecutor.executeFindScope(
            scope = Scope(
                entityType = EntityType.File,
                text = "tagName = \"$randomString\" AND tagValue = \"test\""
            ),
            prevSelectedFilenames = null
        )

        expectThat(response.type).isEqualTo(ResponseType.FILENAMES)
        expectThat(response.responseObject).hasSize(1).containsExactly(file1.absolutePath)
    }

    @Test
    fun `should handle decimal numbers in numeric comparisons`() = fileTestUtils.withTempFiles(count = 3) { (file1, file2, file3) ->
        tagService.addTagToFile(
            location = file1.absolutePath,
            tag = TagInput(name = randomString, value = "8.5")
        )

        tagService.addTagToFile(
            location = file2.absolutePath,
            tag = TagInput(name = randomString, value = "11.2")
        )

        tagService.addTagToFile(
            location = file3.absolutePath,
            tag = TagInput(name = randomString, value = "7.9")
        )

        val response = dslCommandExecutor.executeFindScope(
            scope = Scope(
                entityType = EntityType.File,
                text = "tagName = \"$randomString\" AND tagValue > 8"
            ),
            prevSelectedFilenames = null
        )

        expectThat(response.type).isEqualTo(ResponseType.FILENAMES)
        expectThat(response.responseObject).hasSize(2)
            .containsExactlyInAnyOrder(file1.absolutePath, file2.absolutePath)
    }

    @Test
    fun `should exclude tags with empty or whitespace values in numeric comparisons`() = fileTestUtils.withTempFiles(count = 3) { (file1, file2, file3) ->
        tagService.addTagToFile(
            location = file1.absolutePath,
            tag = TagInput(name = randomString, value = "10")
        )

        tagService.addTagToFile(
            location = file2.absolutePath,
            tag = TagInput(name = randomString, value = "")
        )

        tagService.addTagToFile(
            location = file3.absolutePath,
            tag = TagInput(name = randomString, value = "  ")
        )

        val response = dslCommandExecutor.executeFindScope(
            scope = Scope(
                entityType = EntityType.File,
                text = "tagName = \"$randomString\" AND tagValue > 5"
            ),
            prevSelectedFilenames = null
        )

        expectThat(response.type).isEqualTo(ResponseType.FILENAMES)
        expectThat(response.responseObject).hasSize(1).containsExactly(file1.absolutePath)
    }
}