package com.graphle.graphlemanager.file

/**
 * Response object for file move operations
 * @property from Original file location
 * @property to New file location
 */
data class MoveFileResponse(val from: AbsolutePathString, val to: AbsolutePathString)
