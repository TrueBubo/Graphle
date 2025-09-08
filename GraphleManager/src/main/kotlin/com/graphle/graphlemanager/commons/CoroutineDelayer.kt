package com.graphle.graphlemanager.commons

import kotlin.time.Duration

class CoroutineDelayer : IDelayer {
    override suspend fun delay(duration: Duration) {
        delay(duration)
    }
}