package com.graphle.graphlemanager.file

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.time.Duration.Companion.minutes

/**
 * Used to clean the system of files which do not exist
 * Helps if the file is manipulated outside the system
 * @param fileRepository Repository used for cleaning the database
 */
@Service
class FileSweeper(private val fileRepository: FileRepository) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private fun sweep() {
        fileRepository
            .findAll()
            .map(File::location)
            .filter { !Files.exists(Path(it)) }
            .forEach { fileRepository.removeFileByLocation(it) }
    }

    fun startSweeping() {
        scope.launch {
            while (true) {
                delay(10.minutes)
                sweep()
            }
        }
    }
}