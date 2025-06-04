package com.graphle.graphlemanager.tag

import org.springframework.lang.NonNull
import org.springframework.stereotype.Controller

@Controller
class TagController(private val tagService: TagService) {
    fun tagsByFileLocation(@NonNull location: String): List<Tag> =
        tagService.tagsByFileLocation(location)
}