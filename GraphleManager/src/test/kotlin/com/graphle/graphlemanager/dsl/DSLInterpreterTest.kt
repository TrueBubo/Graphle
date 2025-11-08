package com.graphle.graphlemanager.dsl

import com.graphle.graphlemanager.FileTestUtils
import com.graphle.graphlemanager.connection.ConnectionInput
import com.graphle.graphlemanager.connection.ConnectionService
import com.graphle.graphlemanager.sweeper.Neo4JSweeper
import com.graphle.graphlemanager.tag.TagInput
import com.graphle.graphlemanager.tag.TagService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.neo4j.core.Neo4jClient
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import kotlin.test.AfterTest
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DSLInterpreterTest {
    private val randomString = (1..20).map { ('a'..'z').random() }.joinToString("")

    @Autowired
    private lateinit var neo4jClient: Neo4jClient

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
    fun `should return file matching the file scope command`() = fileTestUtils.withTempFiles(count = 2) { (file1, file2) ->
        val interpreter = DSLInterpreter(neo4jClient)
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
        val results = interpreter.executeScope(
            Scope(
                entityType = EntityType.File,
                text = "location = \"${file1.absolutePath}\" or tagName = \"${randomString}\"",
            )
        )
        expectThat(results).hasSize(2).and { containsExactly(Filename(file1.absolutePath), Filename(file2.absolutePath)) }
    }

    @Test
    fun `should return file connected via the relationship command`() = fileTestUtils.withTempFiles(count = 2) { (file1, file2) ->
        val interpreter = DSLInterpreter(neo4jClient)
        connectionService.addConnection(
            ConnectionInput(
                name = "",
                value = null,
                from = file1.absolutePath,
                to = file2.absolutePath,
                bidirectional = false
            )
        )

        interpreter.executeScope(scope = Scope(entityType = EntityType.Relationship, text = "name"))



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
        val interpreter = DSLInterpreter(neo4jClient)
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
        val interpreter = DSLInterpreter(neo4jClient)
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
        val interpreter = DSLInterpreter(neo4jClient)
        val scope = Scope(
            entityType = EntityType.File,
            text = """tagName = "year" AND tagValue > 2015 AND (location != "/home/(Person)")"""
        )
        expectThat(interpreter.convertScopeToCommand(scope))
            .isEqualTo(
                "MATCH (f:File) OPTIONAL MATCH (f)-[:HasTag]-(t:Tag) " +
                        "WITH f, t WHERE t.name = \"year\" AND t.value > 2015 AND ( f.location <> \"/home/(Person)\" ) RETURN f"
            )
    }
}