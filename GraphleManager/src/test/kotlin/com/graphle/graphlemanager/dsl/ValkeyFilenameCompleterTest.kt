package com.graphle.graphlemanager.dsl

import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class ValkeyFilenameCompleterTest {
    @Test
    fun `filenames`() {
        ValkeyFilenameCompleter.insert(listOf("home", "bubo"))
        ValkeyFilenameCompleter.insert(listOf("home" , "bubo", "notThere"))
        println(ValkeyFilenameCompleter.lookup("/hom", 10))
    }
}