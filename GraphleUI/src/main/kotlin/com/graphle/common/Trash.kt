package com.graphle.common

import com.graphle.common.model.DisplayedSettings
import com.graphle.dialogs.ErrorMessage
import com.graphle.file.util.FileFetcher
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

/**
 * Manages trash operations for files and directories.
 */
object Trash {
    /**
     * Path to the application's trash directory.
     */
    val TRASH_DIR_PATH = Path(userHome, ".graphle-trash")

    /**
     * Ensures the trash directory exists and is available.
     *
     * @return true if trash is available, false otherwise
     */
    private fun ensureTrashAvailable() =
        if (TRASH_DIR_PATH.toFile().exists()) true
        else if (TRASH_DIR_PATH.toFile().mkdirs()) true else false

    /**
     * Moves a file or directory to the trash.
     *
     * @param path Path to the file or directory to move to trash
     */
    suspend fun moveToTrash(path: Path) {
        if (!ensureTrashAvailable()) {
            ErrorMessage.set(
                showErrorMessage = true,
                errorMessage = "Trash is not available"
            )
            return
        }
        apolloClient.moveFile(
            locationFrom = path.absolutePathString(),
            locationTo = TRASH_DIR_PATH.resolve(path.fileName).absolutePathString()
        )
    }

    /**
     * Opens the trash directory in the file viewer.
     *
     * @param setDisplayedSettings Callback to update the displayed settings with trash contents
     */
    suspend fun openTrash(setDisplayedSettings: (DisplayedSettings) -> Unit) {
        if (!ensureTrashAvailable()) {
            ErrorMessage.set(
                showErrorMessage = true,
                errorMessage = "Trash is not available"
            )
            return
        }
        FileFetcher.fetch(
            location = TRASH_DIR_PATH.absolutePathString(),
            onResult = {
                setDisplayedSettings(it)
            }
        )
    }
}