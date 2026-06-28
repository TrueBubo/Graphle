package com.graphle.graphlemanager.commons

import kotlin.time.Duration

/**
 * Implementation of IDelayer using coroutine-based delays.
 * Uses Kotlin's coroutine delay mechanism for suspending execution.
 */
class CoroutineDelayer : IDelayer {
    /**
     * Suspends execution for the specified duration.
     * @param duration The amount of time to delay
     */
    override suspend fun delay(duration: Duration) {
        kotlinx.coroutines.delay(duration)
    }
}