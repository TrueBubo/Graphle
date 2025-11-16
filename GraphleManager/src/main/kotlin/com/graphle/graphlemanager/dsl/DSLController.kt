package com.graphle.graphlemanager.dsl

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class DSLRequest(val command: String)

@RestController
@RequestMapping("/dsl")
class DSLController(private val interpreter: DSLInterpreter) {
    @PostMapping
    fun interpret(@RequestBody request: DSLRequest): DSLResponse {
        return interpreter.interpret(request.command)
    }
}