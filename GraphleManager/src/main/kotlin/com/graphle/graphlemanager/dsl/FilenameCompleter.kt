package com.graphle.graphlemanager.dsl

import com.graphle.graphlemanager.file.AbsolutePathString
import org.springframework.beans.factory.annotation.Value
import java.io.File
import java.nio.file.Files
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.io.path.Path
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds


typealias FilenameComponents = List<String>

@JvmInline
value class CharacterKey(val key: String)

private const val LAST_KEY = "last" // Key of where we ended in database so we do not overwrite previous actions
private const val ROOT_INDEX_KEY = 0L
private const val TRUE_CHAR = "1" // Compact representation of "true" so we do not waste space in database
private val root = CharacterKey(ROOT_INDEX_KEY.toString()) // Every lookup call will start from here

@Value("\${cache.ttl}")
private val ttl = 30.days // Keys not accessed for this long will be removed to prevent
private val fileExistenceTtl = 2.seconds
private const val FILE_EXISTS_TIMEOUT_MILLIS = 20L
private const val FILE_EXISTS_THREADS = 2
private const val LOOKUP_TIME_BUDGET_NANOS = 250_000_000L
private const val MAX_LOOKUP_VISITED_NODES = 2_000
private const val MAX_LOOKUP_EXISTENCE_CHECKS = 100
private val fileExistsThreadId = AtomicInteger()
private val fileExistsExecutor = ThreadPoolExecutor(
    0,
    FILE_EXISTS_THREADS,
    30L,
    TimeUnit.SECONDS,
    SynchronousQueue<Runnable>(),
) { runnable ->
    Thread(runnable, "graphle-file-exists-${fileExistsThreadId.incrementAndGet()}").apply {
        isDaemon = true
    }
}

private class LookupBudget {
    private val startedAtNanos = System.nanoTime()
    private var visitedNodes = 0
    private var existenceChecks = 0

    fun shouldStop(collectedSize: Int, limit: Int): Boolean =
        collectedSize >= limit ||
            visitedNodes >= MAX_LOOKUP_VISITED_NODES ||
            existenceChecks >= MAX_LOOKUP_EXISTENCE_CHECKS ||
            System.nanoTime() - startedAtNanos >= LOOKUP_TIME_BUDGET_NANOS

    fun canVisitNode(collectedSize: Int, limit: Int): Boolean {
        if (shouldStop(collectedSize, limit)) return false
        visitedNodes++
        return true
    }

    fun canCheckFileExists(collectedSize: Int, limit: Int): Boolean {
        if (shouldStop(collectedSize, limit)) return false
        existenceChecks++
        return true
    }
}

/**
 * Stores and finds possible filenames
 * Utilizes trie to efficiently store all the filenames with small memory footprint. Each node has an index as a key,
 * and values stored in trie refer to these keys. Only bottom level and the full path are stored. The bottom level refers
 * to full path so it can be returned.
 * @param storage Structure where the trie is saved to
 */
class FilenameCompleter(
    private val rootStorage: Storage,
) {
    private val sessionStorage = ThreadLocal<Storage>()
    private val storage: Storage get() = sessionStorage.get() ?: rootStorage
    private var lastElement =
        rootStorage.get(LAST_KEY)?.toLong() ?: ROOT_INDEX_KEY // Will insert new elements  at this index

    // Caches so we do not always look in the database which is much more expensive than this
    private val childrenCache = ConcurrentCache<String, Map<String, String>>(ttl)
    private val previousLevelCache = ConcurrentCache<String, MutableSet<String>>(ttl)
    private val treeParentCache = ConcurrentCache<String, String>(ttl)
    private val valueCache = ConcurrentCache<String, String>(ttl)
    private val fullPathEndCache = ConcurrentCache<String, String>(ttl)
    private val fileExistsCache = ConcurrentCache<String, Boolean>(fileExistenceTtl)

    private fun <T> withStorageSession(action: () -> T): T {
        val previousStorage = sessionStorage.get()
        return rootStorage.withSession { storage ->
            sessionStorage.set(storage)
            try {
                action()
            } finally {
                if (previousStorage == null) sessionStorage.remove()
                else sessionStorage.set(previousStorage)
            }
        }
    }

    // Getter functions for keys related to a main node
    private fun keyOfPreviousLevel(key: CharacterKey): CharacterKey = CharacterKey("${key.key}:parents")
    private fun keyOfTreeParent(key: CharacterKey): CharacterKey = CharacterKey("${key.key}:prev")
    private fun keyOfValue(key: CharacterKey): CharacterKey = CharacterKey("${key.key}:val")
    private fun keyOfFullPathEnd(key: CharacterKey): CharacterKey = CharacterKey("${key.key}:full")

    private fun cachedFileExists(filename: AbsolutePathString): Boolean {
        fileExistsCache[filename]?.let { return it }
        val exists = fastFileExists(filename)
        fileExistsCache[filename] = exists
        return exists
    }

    private fun fastFileExists(filename: AbsolutePathString): Boolean {
        val future = try {
            fileExistsExecutor.submit<Boolean> { Files.exists(Path(filename)) }
        } catch (_: RejectedExecutionException) {
            return true
        }

        return try {
            future.get(FILE_EXISTS_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
        } catch (_: TimeoutException) {
            future.cancel(true)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun fullPathAtKey(key: CharacterKey): String? {
        val storedValue = storage.getEx(keyOfFullPathEnd(key).key, ttl) { fullPathEndCache[it] } ?: return null
        return if (storedValue == TRUE_CHAR) {
            findRouteToKey(key)?.also { fullPathEndCache[keyOfFullPathEnd(key).key] = it }
        } else storedValue
    }

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
        collected: MutableSet<String> = mutableSetOf(),
        fileExistsPredicate: (AbsolutePathString) -> Boolean
    ): List<String> = filenameDFS(
        key = key,
        limit = limit,
        collected = collected,
        fileExistsPredicate = fileExistsPredicate,
        budget = LookupBudget(),
    )

    private fun filenameDFS(
        key: CharacterKey,
        limit: Int,
        collected: MutableSet<String>,
        fileExistsPredicate: (AbsolutePathString) -> Boolean,
        budget: LookupBudget,
    ): List<String> {
        if (!budget.canVisitNode(collected.size, limit)) return collected.take(limit)

        val children = storage.hgetAllEx(key.key, ttl) { childrenCache[it] }

        if (collected.size < limit) {
            fullPathAtKey(key)?.let {
                if (budget.canCheckFileExists(collected.size, limit) && fileExistsPredicate(it)) collected.add(it)
            }
        }
        val previousLevelKeys = storage.smembersex(keyOfPreviousLevel(key).key, ttl) { previousLevelCache[it] }
        for (parentKey in previousLevelKeys) {
            if (budget.shouldStop(collected.size, limit)) break
            val route = fullPathAtKey(CharacterKey(parentKey)) ?: continue
            if (route !in collected && budget.canCheckFileExists(collected.size, limit) && fileExistsPredicate(route)) {
                collected.add(route)
            }
        }


        for ((_, charKey) in children) {
            if (budget.shouldStop(collected.size, limit)) break
            val childKey = CharacterKey(charKey)
            filenameDFS(childKey, limit, collected, fileExistsPredicate, budget)
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
            storage.setEx(keyOfFullPathEnd(currNode).key, ttl, TRUE_CHAR) { key, _ ->
                fullPathEndCache[key] = component
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
        withStorageSession {
            insertWithStorageSession(filename)
        }
    }

    private fun insertWithStorageSession(filename: FilenameComponents) {
        if (filename == emptyList<AbsolutePathString>()) return
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
    fun lookup(filenamePrefix: String,
               limit: Int = COMPLETIONS_LIMIT,
               fileExistsPredicate: (AbsolutePathString) -> Boolean = ::cachedFileExists,
               ): List<FilenameComponents> = withStorageSession {
        if (lastElement == ROOT_INDEX_KEY) storage.set(LAST_KEY, ROOT_INDEX_KEY.toString())

        var currNode = root
        var currNodeChildren = storage.hgetAllEx(currNode.key, ttl) { childrenCache[it] }

        filenamePrefix.forEach { character ->
            val char = character.toString()
            val index = currNodeChildren[char] ?: return@withStorageSession emptyList()

            currNode = CharacterKey(index)
            currNodeChildren = storage.hgetAllEx(currNode.key, ttl) { childrenCache[it] }
        }

        try {
            filenameDFS(currNode, limit, fileExistsPredicate = fileExistsPredicate).map {
                val list = it.split(File.separator)
                if (list.first() == "") list.drop(1) else list
            }
        } catch (_: ClassCastException) {
            emptyList()
        }
    }
}
