package com.graphle.common.model

/**
 * Display mode for the main application body.
 */
enum class DisplayMode {
    /** Display a single file with its tags and connections */
    File,
    /** Display files filtered by a specific tag */
    FilesWithTag,
    /** Display a list of filenames */
    Filenames,
    /** Display file connections/relationships */
    Connections,
}