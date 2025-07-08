package com.graphle.graphlemanager.tag

import org.springframework.stereotype.Service

@Service
class TagService(private val tagRepository: TagRepository) {
    fun tagsByFileLocation(fileLocation: String): List<Tag> = tagRepository.tagsByFileLocation(fileLocation)

    fun addTagToFile(fileLocation: String, tag: TagInput): Tag =
        if (tag.value != null) tagRepository.addTagToFile(fileLocation, tag.name, tag.value)
        else tagRepository.addTagToFile(fileLocation, tag.name)
}
