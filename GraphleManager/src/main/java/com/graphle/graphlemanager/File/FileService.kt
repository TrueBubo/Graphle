package com.graphle.graphlemanager.File

import com.graphle.graphlemanager.Tag.Tag
import org.springframework.lang.NonNull
import org.springframework.stereotype.Service

@Service
class FileService(@param:NonNull private val fileRepository: FileRepository) {
    @NonNull
    fun tagsForFileLocation(@NonNull location: String): MutableList<Tag> {
        return fileRepository.tagsByFileLocation(location)
    }
}
