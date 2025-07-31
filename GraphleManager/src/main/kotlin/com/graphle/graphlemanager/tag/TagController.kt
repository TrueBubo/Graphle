package com.graphle.graphlemanager.tag

import com.graphle.graphlemanager.file.File
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class TagController(private val tagService: TagService) {
    @QueryMapping
    fun tagsByFileLocation(@Argument location: String): List<Tag> =
        tagService.tagsByFileLocation(location)

    @MutationMapping
    fun addTagToFile(@Argument location: String, @Argument tag: TagInput): Tag {
        tagService.addTagToFile(location, tag)
        return Tag(tag.name, tag.value)
    }

    @QueryMapping
    fun filesByTag(@Argument tagName: String): List<String> =
        tagService.filesByTag(tagName)
}