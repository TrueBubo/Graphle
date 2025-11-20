package com.graphle.graphlemanager

fun randomString(length: Int): String = (1..20).map { ('a'..'z').random() }.joinToString("")