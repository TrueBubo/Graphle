package com.graphle.graphlemanager

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class GraphleManagerApplication

fun main(args: Array<String>) {
    runApplication<GraphleManagerApplication>(*args)
}