package com.graphle.graphlemanager.time

import java.lang.System.currentTimeMillis

data class TimeRange(val from: Long = 0, val to: Long = currentTimeMillis())
