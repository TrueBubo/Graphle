package com.graphle.graphlemanager.tag

import org.springframework.lang.NonNull
import org.springframework.stereotype.Service

@Service
class TagService(private val tagRepository: TagRepository) {
    fun tagsForFileLocation(@NonNull location: String): List<Tag> {
        return tagRepository.tagsByFileLocation(location)
    }
}
