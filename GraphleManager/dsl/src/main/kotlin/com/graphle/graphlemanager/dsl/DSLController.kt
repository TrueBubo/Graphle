package com.graphle.graphlemanager.dsl

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Request object for DSL command execution.
 * @property command The DSL command string to be interpreted
 */
data class DSLRequest(val command: String)

/**
 * REST controller for handling DSL command requests.
 * @param interpreter The DSL interpreter service used to process commands
 */
@RestController
@RequestMapping("/dsl")
class DSLController(private val interpreter: DSLInterpreter) {
    /**
     * Interprets and executes a DSL command.
     * @param request The request containing the DSL command
     * @return The response from executing the command
     */
    @PostMapping
    fun interpret(@RequestBody request: DSLRequest): DSLResponse {
        return interpreter.interpret(request.command)
    }
}