package com.graphle.graphlemanager.dsl

import com.graphle.graphlemanager.commons.IDelayer
import kotlinx.coroutines.yield
import kotlin.time.Duration

class MockDelayer : IDelayer {
    private var time = 0L
    private var lock = false

    override suspend fun delay(duration: Duration) {
        while (time < duration.inWholeSeconds) {
            yield()
        }
    }

    fun forwardTime(time: Duration) {
        this.time += time.inWholeSeconds
    }
}