package com.graphle.graphlemanager.dsl


typealias FilenameComponents = List<String>

interface FilenameCompleter {
    /**
     * Saves the filename for later retrieval
     * @param filename Components of the filenames consists of parent directories and the bottom level filename itself
     */
    fun insert(filename: FilenameComponents)

    /**
     * Looks up the prefix and returns up to $limit matching filenames
     * @param filenamePrefix Bottom level filename prefix
     * @param limit Returns at most this many filenames
     * @return list of possible filenames
     */
    fun lookup(filenamePrefix: String, limit: Int = 10): List<FilenameComponents>
}