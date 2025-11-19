import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.graphle.common.model.DisplayedSettings
import com.graphle.common.supervisorIoScope
import com.graphle.common.ui.Pill
import com.graphle.file.components.FileMenu
import com.graphle.file.util.FileFetcher
import kotlinx.coroutines.launch

/**
 * Displays a filename as a clickable pill with a context menu.
 *
 * @param filename The filename to display
 * @param onResult Callback when the file is clicked
 * @param onRefresh Callback to refresh the view
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FilenameBox(
    filename: String,
    onResult: (DisplayedSettings?) -> Unit,
    onRefresh: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    Box(Modifier.padding(bottom = 10.dp)) {
        Row {
            Pill(
                texts = listOf(filename),
                onClick = {
                    supervisorIoScope.launch {
                        FileFetcher.fetch(
                            location = filename,
                            onResult = onResult,
                        )
                    }
                },
                onRightClick = {
                    showMenu = true
                }
            )
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
            ) {
                FileMenu(
                    location = filename,
                    setShowMenu = { showMenu = it },
                    onRefresh = onRefresh,
                )
            }
        }
    }
}

