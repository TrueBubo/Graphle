package com.graphle.graphlemanager
import com.graphle.graphlemanager.file.FileService
import org.springframework.stereotype.Component
import java.io.File
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempFile

@Component
class FileTestUtils(val fileService: FileService) {
    fun withTempFiles(count: Int = 1, block: (List<File>) -> Unit) {
        val files = (0 until count).map { File(createTempFile().absolutePathString()) }
        try {
            block(files)
        } finally {
            files.forEach { fileService.removeFile(it.absolutePath) }
        }
    }
}