package com.graphle

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
import kotlinx.coroutines.launch


@Composable
fun AppMenu(
    showAppMenu: Boolean,
    setShowAppMenu: (Boolean) -> Unit,
    onResult: (DisplayedData?) -> Unit,
    location: String,
    setLocation: (String) -> Unit,
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
                                setLocation(userHome)
                            }
                        )
                    }
                }
            )

            Divider()

            FileMenu(
                location = location,
                connection = null,
                setShowMenu = setShowAppMenu,
                onRefresh = {
                    supervisorIoScope.launch {
                        FileFetcher.fetch(
                            location = location,
                            onResult = { displayedInfo ->
                                onResult(
                                    DisplayedData(
                                        tags = displayedInfo?.tags ?: emptyList(),
                                        connections = displayedInfo?.connections ?: emptyList(
                                        )
                                    )
                                )
                            }
                        )
                    }
                }
            )

            Divider()

            DropdownMenuItem(
                content = { Text("Open Trash") },
                onClick = {
                    setShowAppMenu(false)
                    supervisorIoScope.launch {
                        Trash.openTrash(setDisplayedData = onResult, setLocation = setLocation)
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
                            location = location,
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
                    supervisorIoScope.launch {
                        FileFetcher.fetch(
                            location = location,
                            onResult = onResult
                        )
                    }
                }
            )
        }
    }