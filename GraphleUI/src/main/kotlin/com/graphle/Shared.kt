package com.graphle

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

val supervisorIoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)