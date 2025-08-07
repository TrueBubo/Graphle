package com.graphle.graphlemanager.sweeper

import com.graphle.graphlemanager.file.File
import com.graphle.graphlemanager.file.FileRepository
import com.graphle.graphlemanager.tag.TagRepository
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
 * Used to clean the system of nodes which do not exist
 * Helps if the file is manipulated outside the system or to clean up orphaned nodes
 * @param fileRepository Repository used for cleaning the database
 */
@Service
class Neo4JSweeper(private val fileRepository: FileRepository, private val tagRepository: TagRepository) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val sweepInterval = 1.minutes

    init {
        scope.launch { startSweeping() }
    }

    private fun sweep() {
        fileRepository
            .findAll()
            .map(File::location)
            .filter { !Files.exists(Path(it)) }
            .forEach { fileRepository.removeFileByLocation(it) }
        tagRepository.removeOrphanTags()
    }

    private fun startSweeping() {
        scope.launch {
            while (true) {
                delay(sweepInterval)
                sweep()
            }
        }
    }
}