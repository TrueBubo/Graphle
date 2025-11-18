package com.graphle.common

import com.graphle.common.model.DisplayedSettings
import com.graphle.dialogs.ErrorMessage
import com.graphle.file.util.FileFetcher
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

object Trash {
    val TRASH_DIR_PATH = Path(userHome, ".graphle-trash")

    private fun ensureTrashAvailable() =
        if (TRASH_DIR_PATH.toFile().exists()) true
        else if (TRASH_DIR_PATH.toFile().mkdirs()) true else false

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