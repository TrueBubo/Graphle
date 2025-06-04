package com.graphle.graphlemanager.tag

import org.springframework.stereotype.Service

@Service
class TagService(private val tagRepository: TagRepository) {
    fun tagsByFileLocation(location: String): List<Tag> {
        return tagRepository.tagsByFileLocation(location)
    }
}
