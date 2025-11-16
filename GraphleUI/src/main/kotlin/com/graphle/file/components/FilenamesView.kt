import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.text.font.FontWeight
import com.graphle.common.model.DisplayedSettings
import com.graphle.common.supervisorIoScope
import com.graphle.dialogs.ErrorMessage
import com.graphle.dsl.DSLHistory
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun FilenamesView(
    displayedSettings: DisplayedSettings,
    setDisplayedSettings: (DisplayedSettings) -> Unit,
) {
    displayedSettings.data?.filenames
        ?.apply { Text(text = "Files", fontWeight = FontWeight.Bold) }
        ?.let { filenames ->
            Column {
                filenames.forEach { filename ->
                    FilenameBox(
                        filename = filename,
                        onResult = {
                            if (it == null) {
                                ErrorMessage.set(
                                    showErrorMessage = true,
                                    errorMessage = "Failed to load ${filename}, " +
                                            "check the file exists and you have permission to read it.",
                                )
                            } else {
                                setDisplayedSettings(it)
                            }
                        },
                        onRefresh = {
                            supervisorIoScope.launch {
                                DSLHistory.repeatLastDisplayedCommand(setDisplayedSettings)
                            }
                        },
                    )
                }
            }
        }
}