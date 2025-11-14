package com.graphle.graphlemanager.tag

import com.graphle.graphlemanager.BaseGraphQlIntegrationTest
import com.graphle.graphlemanager.FileTestUtils
import com.graphle.graphlemanager.sweeper.Neo4JSweeper
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.first
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import kotlin.test.Test

class TagIntegrationTests(
    @Autowired private val tagService: TagService,
    @Autowired private val fileTestUtils: FileTestUtils,
    @Autowired private val neo4JSweeper: Neo4JSweeper
) : BaseGraphQlIntegrationTest() {
    @Test
    fun `insert tag`() = fileTestUtils.withTempFiles { files ->
        try {
            post { insertTagQuery(location = files[0].absolutePath) }
        } finally {
            tagService.removeTag(files[0].absolutePath, TagInput(name = "exampleTag", value = "exampleValue"))
        }
    }

    @Test
    fun `tag was inserted and fetched`() = fileTestUtils.withTempFiles { files ->
        try {
            post { insertTagQuery(location = files[0].absolutePath) }
            expectThat(fetchListPost<Tag> { fetchTagQuery(location = files[0].absolutePath) })
                .hasSize(1)
                .first()
                .and {
                    get { name }.isEqualTo("tmp_exampleTag")
                    get { value }.isEqualTo("tmp_exampleValue")
                }
        } finally {
            tagService.removeTag(files[0].absolutePath, TagInput(name = "tmp_exampleTag", value = "tmp_exampleValue"))
        }
    }


    private fun insertTagQuery(location: String) = mutation {
        addTagToFile(location, Tag("tmp_exampleTag", "tmp_exampleValue"))
    }

    private fun fetchTagQuery(location: String) = query {
        tagsByFileLocation(location)
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