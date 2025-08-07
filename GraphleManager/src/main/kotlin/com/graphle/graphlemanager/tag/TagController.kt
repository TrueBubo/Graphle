package com.graphle.graphlemanager.tag

import com.graphle.graphlemanager.file.AbsolutePathString
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

/**
 * GraphQL controller for managing information related to tags
 * @param tagService Service used for retrieving required information
 * */
@Controller
class TagController(private val tagService: TagService) {
    /**
     * Retrieves the list of all the tags for a file at a particular location
     * @param location Absolute path of the file in server filesystem
     * @return Tags corresponding to the file
     */
    @QueryMapping
    fun tagsByFileLocation(@Argument location: AbsolutePathString): List<Tag> =
        tagService.tagsByFileLocation(location)

    /**
     * Marks the given file with a tag via a connection to a tag node
     * @param location Absolute path of the file in server filesystem
     * @param tag Tag the system associate will with the file
     * @return Inserted tag
     */
    @MutationMapping
    fun addTagToFile(@Argument location: AbsolutePathString, @Argument tag: TagInput): Tag {
        tagService.addTagToFile(location, tag)
        return Tag(tag.name, tag.value)
    }

    /**
     * Retrieves the absolute paths of all the files containing the given tag
     * @param tagName name of tag to search for
     * @return Absolute paths of files with tag with the given name
     */
    @QueryMapping
    fun filesByTag(@Argument tagName: String): List<String> =
        tagService.filesByTag(tagName)
}