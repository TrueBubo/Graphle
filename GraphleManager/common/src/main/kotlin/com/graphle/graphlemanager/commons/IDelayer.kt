package com.graphle.graphlemanager.commons

import kotlin.time.Duration

/**
 * Interface for delaying execution.
 * Provides an abstraction for suspendable delay operations, useful for testing and mocking.
 */
interface IDelayer {
    /**
     * Suspends the current coroutine for the specified duration.
     * @param duration The amount of time to delay execution
     */
    suspend fun delay(duration: Duration)
}