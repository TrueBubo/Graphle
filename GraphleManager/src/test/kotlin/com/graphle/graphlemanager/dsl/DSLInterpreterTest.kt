package com.graphle.graphlemanager.dsl

import com.graphle.graphlemanager.FileTestUtils
import com.graphle.graphlemanager.connection.Connection
import com.graphle.graphlemanager.connection.ConnectionInput
import com.graphle.graphlemanager.connection.ConnectionService
import com.graphle.graphlemanager.file.FileController
import com.graphle.graphlemanager.file.FileService
import com.graphle.graphlemanager.sweeper.Neo4JSweeper
import com.graphle.graphlemanager.tag.TagInput
import com.graphle.graphlemanager.tag.TagService
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.neo4j.core.Neo4jClient
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.containsExactly
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.first
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import kotlin.test.AfterTest
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DSLInterpreterTest {
    private val randomString = (1..20).map { ('a'..'z').random() }.joinToString("")

    @Autowired
    private lateinit var neo4jClient: Neo4jClient

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var fileController: FileController

    @Autowired
    private lateinit var neo4JSweeper: Neo4JSweeper

    @Autowired
    private lateinit var connectionService: ConnectionService

    @Autowired
    private lateinit var tagService: TagService

    @Autowired
    private lateinit var fileTestUtils: FileTestUtils

    @AfterTest
    fun tearDown() {
        neo4JSweeper.sweep()
    }

    @Test
    fun `should execute using the whole command for hierarchical connection`() =
        fileTestUtils.withTempFiles(count = 2) { (file1, file2) ->
            val interpreter = DSLInterpreter(neo4jClient, fileService, connectionService, tagService, fileController)
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
            expectThat(response).isNotNull().and {
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
            val interpreter = DSLInterpreter(neo4jClient, fileService, connectionService, tagService, fileController)
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
            expectThat(response).isNotNull().and {
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
            val interpreter = DSLInterpreter(neo4jClient, fileService, connectionService, tagService, fileController)
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

            val response =
                interpreter.interpret("find (location = \"$file1\")[name = \"$randomString\"]()[name = \"${randomString}2\"]()[pred]()")
            expectThat(response).isNotNull().and {
                get { type }.isEqualTo(ResponseType.FILENAMES)
                get { responseObject }.hasSize(1).first().isEqualTo((file3.parent))
            }
        }
    }

    @Test
    fun `invalid file syntax returns error response`() {
        val interpreter = DSLInterpreter(neo4jClient, fileService, connectionService, tagService, fileController)
        val response = interpreter.interpret("find (location = \" or tagName = \"${randomString}\")")

        expectThat(response).isNotNull().and {
            get { type }.isEqualTo(ResponseType.ERROR)
            get { responseObject }.hasSize(1).first().contains("Unable to parse")
        }
    }

    @Test
    fun `invalid connection syntax returns error response`() {
        val interpreter = DSLInterpreter(neo4jClient, fileService, connectionService, tagService, fileController)
        val response = interpreter.interpret("find ()[nonExistent = 2]")
        expectThat(response).isNotNull().and {
            get { type }.isEqualTo(ResponseType.ERROR)
            get { responseObject }.hasSize(1).first().contains("Unable to parse")
        }
    }

    @Test
    fun `should return file matching the file scope command`() =
        fileTestUtils.withTempFiles(count = 2) { (file1, file2) ->
            val interpreter = DSLInterpreter(neo4jClient, fileService, connectionService, tagService, fileController)
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
            val response = interpreter.executeScope(
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
        val interpreter = DSLInterpreter(neo4jClient, fileService, connectionService, tagService, fileController)
        tagService.addTagToFile(
            location = file1.absolutePath,
            tag = TagInput(name = randomString, value = "8")
        )
        tagService.addTagToFile(
            location = file2.absolutePath,
            tag = TagInput(name = randomString, value = "11.2")
        )
        val response = interpreter.executeScope(
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
            val interpreter = DSLInterpreter(neo4jClient, fileService, connectionService, tagService, fileController)
            connectionService.addConnection(
                ConnectionInput(
                    name = randomString,
                    value = null,
                    from = file1.absolutePath,
                    to = file2.absolutePath,
                    bidirectional = false
                )
            )

            val response = interpreter.executeScope(
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
        val interpreter = DSLInterpreter(neo4jClient, fileService, connectionService, tagService, fileController)
        val response = interpreter.executeScope(
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
    fun `interpreter splits search command to scopes`() {
        val interpreter = DSLInterpreter(neo4jClient, fileService, connectionService, tagService, fileController)
        val command = """(tagName = "name")[name = "friend"](location = "/home/(Person)")"""
        val scopes = interpreter.splitSearchIntoScopes(command)
        expectThat(scopes).hasSize(3).containsExactly(
            Scope(entityType = EntityType.File, text = """tagName = "name""""),
            Scope(entityType = EntityType.Relationship, text = """name = "friend""""),
            Scope(entityType = EntityType.File, text = """location = "/home/(Person)"""")
        )
    }

    @Test
    fun `interpreter splits search command with priorities`() {
        val interpreter = DSLInterpreter(neo4jClient, fileService, connectionService, tagService, fileController)
        val command = """(tagName = "name" AND (location = "/home"))[name = "friend"](location = "/home/(Person)")"""
        val scopes = interpreter.splitSearchIntoScopes(command)
        expectThat(scopes).hasSize(3).containsExactly(
            Scope(entityType = EntityType.File, text = """tagName = "name" AND (location = "/home")"""),
            Scope(entityType = EntityType.Relationship, text = """name = "friend""""),
            Scope(entityType = EntityType.File, text = """location = "/home/(Person)"""")
        )
    }

    @Test
    fun `interpreter correctly translates file scope into neo4j`() {
        val interpreter = DSLInterpreter(neo4jClient, fileService, connectionService, tagService, fileController)
        val scope = Scope(
            entityType = EntityType.File,
            text = """tagName = "year" AND tagValue > 2015 AND (location != "/home/(Person)")"""
        )
        expectThat(interpreter.convertScopeToCommand(scope = scope, prevSelectedFilename = null))
            .isEqualTo(
                "MATCH (f:File) OPTIONAL MATCH (f)-[:HasTag]-(t:Tag) " +
                        "WITH f, t WHERE t.name = \"year\" AND t.value > 2015 AND ( f.location <> \"/home/(Person)\" ) RETURN f"
            )
    }
}