package com.graphle.graphlemanager.sweeper

import com.graphle.graphlemanager.file.AbsolutePathString
import com.graphle.graphlemanager.file.File
import com.graphle.graphlemanager.file.FileRepository
import com.graphle.graphlemanager.tag.TagRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Used to clean the system of nodes which do not exist
 * Helps if the file is manipulated outside the system or to clean up orphaned nodes
 * @param fileRepository Repository used for cleaning the database
 */
@Service
class Neo4JSweeper(private val fileRepository: FileRepository, private val tagRepository: TagRepository) {
    private var _sweepInterval = 1.minutes

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        startSweeping()
    }

    /**
     * Forces sweeping to be done right now
     * @param filesExistsPredicate Checks whether the file is eligible to be swept
     */
    fun sweep(filesExistsPredicate: (AbsolutePathString) -> Boolean = { path -> Files.exists(Path(path))}) {
        fileRepository
            .findAll()
            .map(File::location)
            .filter { !filesExistsPredicate(it) }
            .forEach { fileRepository.removeFileByLocation(it) }
        tagRepository.removeOrphanTags()
    }

    /**
     * Background task to sweep old data
     * It is ran in the application background
     * @param filesExistsPredicate Checks whether the file is eligible to be swept
     */
    private fun startSweeping(filesExistsPredicate: (AbsolutePathString) -> Boolean = { path -> Files.exists(Path(path)) }) {
        scope.launch {
            while (true) {
                delay(_sweepInterval)
                sweep(filesExistsPredicate)
            }
        }
    }
}