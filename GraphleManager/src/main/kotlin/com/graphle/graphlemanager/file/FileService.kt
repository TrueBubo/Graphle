package com.graphle.graphlemanager.file

import com.graphle.graphlemanager.tag.Tag
import com.graphle.graphlemanager.tag.TagRepository
import org.springframework.lang.NonNull
import org.springframework.stereotype.Service

@Service
class FileService(private val fileRepository: FileRepository, private val tagRepository: TagRepository) {
    @NonNull
    fun tagsForFileLocation(@NonNull location: String): List<Tag> {
        return tagRepository.tagsByFileLocation(location)
    }
}
