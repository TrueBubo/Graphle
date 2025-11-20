package com.graphle.graphlemanager

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Main Spring Boot application class for Graphle Manager.
 * This application manages file relationships, tags, and provides a DSL for querying file metadata.
 */
@SpringBootApplication
open class GraphleManagerApplication

/**
 * Application entry point.
 * Starts the Spring Boot application with the provided command-line arguments.
 * @param args Command-line arguments passed to the application
 */
fun main(args: Array<String>) {
    runApplication<GraphleManagerApplication>(*args)
}