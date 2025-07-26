package com.graphle.graphlemanager.dsl

import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class ValkeyFilenameCompleterTest {
    @Test
    fun `filenames`() {
        valkeyFilenameCompleter.insert(listOf("home", "bubo"))
        valkeyFilenameCompleter.insert(listOf("home" , "bubo", "notThere"))
        println(valkeyFilenameCompleter.lookup("no", 10))
    }
}