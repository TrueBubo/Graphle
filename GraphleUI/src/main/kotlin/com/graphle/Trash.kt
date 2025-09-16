package com.graphle

import kotlinx.io.IOException
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

object Trash {
    val TRASH_DIR_PATH = Path(userHome, ".graphle-trash")

    private fun ensureTrashAvailable() =
        if (TRASH_DIR_PATH.toFile().exists()) true
        else if (TRASH_DIR_PATH.toFile().mkdirs()) true else false

    suspend fun moveToTrash(path: Path) {
        if (!ensureTrashAvailable()) throw IOException("Trash is not available")
        apolloClient.moveFile(
            locationFrom = path.absolutePathString(),
            locationTo = TRASH_DIR_PATH.resolve(path.fileName).absolutePathString()
        )
    }
}