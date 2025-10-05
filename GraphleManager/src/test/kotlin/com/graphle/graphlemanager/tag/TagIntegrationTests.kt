package com.graphle.graphlemanager.tag

import Neo4JBaseIntegrationTest
import kotlin.test.Test

class TagIntegrationTests : Neo4JBaseIntegrationTest() {
    @Test
    fun `insert tag`() {
        post { insertTagQuery }
    }

    @Test
    fun `tag was inserted and fetched`() {
        post { insertTagQuery }
        postListResponse<Tag> { fetchTagQuery }
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