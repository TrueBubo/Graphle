package com.graphle.graphlemanager.tag

import com.graphle.graphlemanager.file.AbsolutePathString
import org.springframework.stereotype.Service

/**
 * Service for managing information related to tags
 * @param tagRepository Repository used for retrieving required information
 */
@Service
class TagService(private val tagRepository: TagRepository) {
    /**
     * Retrieves the list of all the tags for a file at a particular location
     * @param location Absolute path of the file in server filesystem
     * @return Tags corresponding to the file
     */
    fun tagsByFileLocation(location: AbsolutePathString): List<Tag> = tagRepository.tagsByFileLocation(location)

    /**
     * Marks the given file with a tag via a connection to a tag node
     * @param location Absolute path of the file in server filesystem
     * @param tag Tag the system associate will with the file
     * @return Inserted [tag]
     */
    fun addTagToFile(location: AbsolutePathString, tag: TagInput) {
        if (tag.value != null) tagRepository.addTagToFile(location, tag.name, tag.value)
        else {
            println(tag)
            tagRepository.addTagToFile(
                location, tag.name
            )
        }
    }

    /**
     * Deletes the tag from the database
     * @param tag Tag to be removed
     * @return Whether the tag was deleted
     */
    fun removeTag(location: AbsolutePathString, tag: Tag) {
        if (tag.value != null) {
            System.err.println("Removing tag with value: $tag")
            tagRepository.removeTag(location,tag.name, tag.value)
        }
        else tagRepository.removeTag(location, tag.name)
    }

    /**
     * Retrieves the absolute paths of all the files containing the given tag
     * @param tagName name of tag to search for
     * @return Absolute paths of files with tag with the given [tagName]
     */
    fun filesByTag(tagName: String): List<TagForFile> = tagRepository.filesByTag(tagName).map {
        TagForFile(
            location = it.location,
            tag = Tag(
                name = it.tagName!!,
                value = it.tagValue
            )
        )
    }
}
