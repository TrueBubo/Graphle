package com.graphle.graphlemanager.commons

import kotlin.time.Duration

interface IDelayer {
    suspend fun delay(duration: Duration)
}