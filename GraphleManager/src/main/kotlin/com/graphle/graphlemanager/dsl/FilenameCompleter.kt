package com.graphle.graphlemanager.dsl

import java.io.File
import java.nio.file.Files
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.io.path.Path
import kotlin.time.Duration.Companion.days


typealias FilenameComponents = List<String>

@JvmInline
value class CharacterKey(val key: String)

private const val LAST_KEY = "last" // Key of where we ended in database so we do not overwrite previous actions
private const val ROOT_INDEX_KEY = 0L
private const val TRUE_CHAR = "1" // Compact representation of "true" so we do not waste space in database
private val root = CharacterKey(ROOT_INDEX_KEY.toString()) // Every lookup call will start from here
private val ttl = 30.days // Keys not accessed for this long will be removed to prevent

/**
 * Stores and finds possible filenames
 * Utilizes trie to efficiently store all the filenames with small memory footprint. Each node has an index as a key,
 * and values stored in trie refer to these keys. Only bottom level and the full path are stored. The bottom level refers
 * to full path so it can be returned.
 * @param storage Structure where the trie is saved to
 * @param fileExistsPredicate Verification of file existence so the system does not return non existent files
 */
class FilenameCompleter(
    private val storage: Storage,
    private val fileExistsPredicate: (String) -> Boolean = { Files.exists(Path(it)) }
) {
    private var lastElement = storage.get(LAST_KEY)?.toLong() ?: ROOT_INDEX_KEY // Will insert new elements  at this index

    // Caches so we do not always look in the database which is much more expensive than this
    private val childrenCache = ConcurrentCache<String, Map<String, String>>(ttl)
    private val previousLevelCache = ConcurrentCache<String, MutableSet<String>>(ttl)
    private val treeParentCache = ConcurrentCache<String, String>(ttl)
    private val valueCache = ConcurrentCache<String, String>(ttl)
    private val fullPathEndCache = ConcurrentCache<String, String>(ttl)

    // Getter functions for keys related to a main node
    private fun keyOfPreviousLevel(key: CharacterKey): CharacterKey = CharacterKey("${key.key}:parents")
    private fun keyOfTreeParent(key: CharacterKey): CharacterKey = CharacterKey("${key.key}:prev")
    private fun keyOfValue(key: CharacterKey): CharacterKey = CharacterKey("${key.key}:val")
    private fun keyOfFullPathEnd(key: CharacterKey): CharacterKey = CharacterKey("${key.key}:full")

    /**
     * Searches for a component, and if it does not exists it will build a path for it
     * @param component Searching this in trie
     * @return The key the component ends in
     */
    private fun searchAndAddComponent(component: String): CharacterKey {
        if (lastElement == ROOT_INDEX_KEY) storage.set(LAST_KEY, ROOT_INDEX_KEY.toString())

        var currNode = root
        var currNodeChildren = storage.hgetAllEx(currNode.key, ttl) { childrenCache[it] }.toMutableMap()
        component.forEach { character ->
            val char = character.toString()
            val maybeIndex = currNodeChildren[char]
            val index = if (maybeIndex != null) maybeIndex else {
                storage.set(LAST_KEY, lastElement.toString())
                (++lastElement).toString()
            }
            currNodeChildren[char] = index
            storage.hsetex(currNode.key, ttl, currNodeChildren) { key, value -> childrenCache[key] = value }

            storage.setEx(
                keyOfTreeParent(CharacterKey(index)).key,
                ttl,
                currNode.key
            ) { key, value -> treeParentCache[key] = value }
            storage.setEx(keyOfValue(CharacterKey(index)).key, ttl, char) { key, value -> valueCache[key] = value }

            currNode = CharacterKey(index)
            currNodeChildren = storage.hgetAllEx(currNode.key, ttl) { childrenCache[it] }.toMutableMap()
        }

        storage.expire(currNode.key, ttl.inWholeSeconds)

        return currNode
    }

    /**
     * Finds the string which ends in a given key
     * @param key to find a string from root to
     * @return Root to key string or null if it does not exists
     */
    private fun findRouteToKey(key: CharacterKey): String? {
        val route = StringBuilder()
        var lastKey = key
        while (lastKey != root) {
            val parentKeyPointer = keyOfTreeParent(lastKey)
            val parentKey = storage.getEx(parentKeyPointer.key, ttl) { treeParentCache[it] }
            if (parentKey == null) return null

            storage.expire(parentKeyPointer.key, ttl.inWholeSeconds)
            storage.expire(parentKey, ttl.inWholeSeconds)

            val lastKeyChar = storage.getEx(keyOfValue(lastKey).key, ttl) { valueCache[it] }
            if (lastKeyChar == null) return null
            route.append(lastKeyChar)
            lastKey = CharacterKey(parentKey)
        }
        return route.toString().reversed()
    }

    /**
     * Finds filenames starting from the key
     * @param key Starting of DFS
     * @param limit Ends if it already found this many filenames
     * @param collected Used to store the found filenames
     */
    fun filenameDFS(
        key: CharacterKey,
        limit: Int,
        collected: MutableSet<String> = mutableSetOf()
    ): List<String> {
        if (collected.size >= limit) return collected.take(limit)

        val children = storage.hgetAllEx(key.key, ttl) { childrenCache[it] }

        if (collected.size < limit && storage.getEx(
                keyOfFullPathEnd(key).key,
                ttl
            ) { fullPathEndCache[it] } == TRUE_CHAR
        ) {
            findRouteToKey(key)?.let {
                if (fileExistsPredicate(it)) collected.add(it)
            }
        }
        val previousLevelKeys = storage.smembersex(keyOfPreviousLevel(key).key, ttl) { previousLevelCache[it] }
        previousLevelKeys
            .mapNotNull { parentKey -> findRouteToKey(CharacterKey(parentKey)) }
            .filter { it !in collected && fileExistsPredicate(it) }
            .forEach(collected::add)


        for ((_, charKey) in children) {
            if (collected.size >= limit) break
            val childKey = CharacterKey(charKey)
            filenameDFS(childKey, limit, collected)
        }

        return collected.toList()
    }

    /**
     * Inserts the component into trie
     * @param component Component to be inserted
     * @param parent Links there so it can reconstruct the whole path, is null if the component if the full path
     */
    private fun insertComponent(component: String, parent: CharacterKey?): CharacterKey {
        val currNode = searchAndAddComponent(component)
        if (parent == null) {
            storage.setEx(keyOfFullPathEnd(currNode).key, ttl, TRUE_CHAR) { key, value ->
                fullPathEndCache[key] = value
            }
        }
        parent?.let {
            val previousLevelKey = keyOfPreviousLevel(currNode).key
            storage.saddex(previousLevelKey, ttl, parent.key) { key, value ->
                if (previousLevelCache[key] == null) previousLevelCache[key] = mutableSetOf()
                previousLevelCache[key]?.add(value)
            }
        }

        return currNode
    }


    /**
     * Saves the filename for later retrieval
     * @param filename Components of the filenames consists of parent directories and the bottom level filename itself
     */
    fun insert(filename: FilenameComponents) {
        val filenameString = filename.joinToString(prefix = File.separator, separator = File.separator)
        val fullFileKey = insertComponent(filenameString, null)
        insertComponent(filename.last(), fullFileKey)
    }

    /**
     * Looks up the prefix and returns up to $limit matching filenames
     * @param filenamePrefix Bottom level filename prefix
     * @param limit Returns at most this many filenames
     * @return list of possible filenames
     */
    fun lookup(filenamePrefix: String, limit: Int = COMPLETIONS_LIMIT): List<FilenameComponents> {
        if (lastElement == ROOT_INDEX_KEY) storage.set(LAST_KEY, ROOT_INDEX_KEY.toString())

        var currNode = root
        var currNodeChildren = storage.hgetAllEx(currNode.key, ttl) { childrenCache[it] }

        filenamePrefix.forEach { character ->
            val char = character.toString()
            val index = currNodeChildren[char]
            if (index == null) return emptyList()

            storage.expire(keyOfTreeParent(CharacterKey(index)).key, ttl.inWholeSeconds)

            currNode = CharacterKey(index)
            currNodeChildren = storage.hgetAllEx(currNode.key, ttl) { childrenCache[it] }
        }

        return filenameDFS(currNode, limit).map {
            val list = it.split(File.separator)
            if (list.first() == "") list.drop(1) else list
        }
    }
}