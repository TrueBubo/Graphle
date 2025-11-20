package com.graphle.common.model

/**
 * Container for the current display settings and data.
 *
 * @property data The data to be displayed (null if loading or error)
 * @property mode The current display mode
 */
data class DisplayedSettings(
    val data: DisplayedData?,
    val mode: DisplayMode,
)