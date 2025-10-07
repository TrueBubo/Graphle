package com.graphle.graphlemanager.tag

import BaseIntegrationTest
import com.graphle.graphlemanager.sweeper.Neo4JSweeper
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.AfterTest
import kotlin.test.Test

class TagIntegrationTests(
    @Autowired private val neo4JSweeper: Neo4JSweeper
) : BaseIntegrationTest() {
    @AfterTest
    fun tearDown() {
        neo4JSweeper.sweep()
    }

    @Test
    fun `insert tag`() {
        post { insertTagQuery }
    }

    @Test
    fun `tag was inserted and fetched`() {
        post { insertTagQuery }
        fetchListPost<Tag> { fetchTagQuery }
    }

    private val insertTagQuery = mutation {
        addTagToFile("/test", Tag("exampleTag", "exampleValue"))
    }

    private val fetchTagQuery = query {
        tagsByFileLocation("/test")
    }

    companion object {
        private fun addTagToFile(location: String, tag: Tag) = """
          addTagToFile(location: "$location", tag: {name: "${tag.name}"${if (tag.value == null) "" else """, value: "${tag.value}""""}}) {
            name
            value
          }
        """.trimIndent()

        private fun tagsByFileLocation(location: String) = """
          tagsByFileLocation(location: "$location") {
            name
            value
          }
        """.trimIndent()
    }
}