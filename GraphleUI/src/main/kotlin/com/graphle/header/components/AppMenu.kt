package com.graphle.header.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.graphle.common.Trash
import com.graphle.common.model.DisplayMode
import com.graphle.file.util.FileFetcher
import com.graphle.file.components.FileMenu
import com.graphle.common.model.DisplayedData
import com.graphle.common.model.DisplayedSettings
import com.graphle.common.supervisorIoScope
import com.graphle.common.userHome
import com.graphle.dsl.DSLHistory
import kotlinx.coroutines.launch


@Composable
internal fun AppMenu(
    showAppMenu: Boolean,
    setShowAppMenu: (Boolean) -> Unit,
    onResult: (DisplayedSettings) -> Unit,
    getDisplayedSettings: () -> DisplayedSettings,
    setDarkMode: (Boolean) -> Unit,
    getDarkMode: () -> Boolean,
) =
    Box {
        Button(
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface),
            onClick = {
                setShowAppMenu(true)
            },
            modifier = Modifier.height(fieldHeight)
        ) {
            Text("☰")
        }

        DropdownMenu(expanded = showAppMenu, onDismissRequest = { setShowAppMenu(false) }) {
            DropdownMenuItem(
                content = { Text("Open Home") },
                onClick = {
                    setShowAppMenu(false)
                    supervisorIoScope.launch {
                        FileFetcher.fetch(
                            location = userHome,
                            onResult = {
                                onResult(it)
                            }
                        )
                    }
                }
            )

            Divider()

            if (getDisplayedSettings().mode == DisplayMode.File) {
                FileMenu(
                    location = getDisplayedSettings().data?.location ?: "",
                    connection = null,
                    setShowMenu = setShowAppMenu,
                    onRefresh = {
                        supervisorIoScope.launch {
                            DSLHistory.repeatLastDisplayedCommand(onResult)
                        }
                    }
                )
            }

            Divider()

            DropdownMenuItem(
                content = { Text("Open Trash") },
                onClick = {
                    setShowAppMenu(false)
                    supervisorIoScope.launch {
                        Trash.openTrash(setDisplayedSettings = onResult)
                    }
                }
            )

            DropdownMenuItem(
                content = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Show Hidden Files")
                        Spacer(Modifier.weight(1f))
                        Checkbox(
                            checked = FileFetcher.showHiddenFiles,
                            onCheckedChange = null
                        )
                    }
                },
                onClick = {
                    FileFetcher.showHiddenFiles = !FileFetcher.showHiddenFiles

                    setShowAppMenu(false)
                    supervisorIoScope.launch {
                        FileFetcher.fetch(
                            location = getDisplayedSettings().data?.location ?: "",
                            onResult = onResult
                        )
                    }
                }
            )

            DropdownMenuItem(
                content = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Dark mode")
                        Spacer(Modifier.weight(1f))
                        Checkbox(
                            checked = getDarkMode(),
                            onCheckedChange = null
                        )
                    }
                },
                onClick = {
                    setDarkMode(!getDarkMode())
                    setShowAppMenu(false)
                }
            )
        }
    }