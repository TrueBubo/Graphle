package com.graphle.graphlemanager

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest(
    classes = [GraphleManagerApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
open class BaseRestIntegrationTest {
    @Autowired
    private lateinit var _mockMvc: MockMvc

    internal val mockMvc: MockMvc get() = _mockMvc

    private fun postObject(
        url: String,
        status: ResultMatcher = MockMvcResultMatchers.status().isOk,
        content: () -> String
    ): JsonElement {
        val response = post(url, status, content)
        val responseBody = response.response.contentAsString
        return Json.parseToJsonElement(responseBody)
    }

    internal inline fun <reified T> post(
        url: String,
        status: ResultMatcher = MockMvcResultMatchers.status().isOk,
        noinline content: () -> String
    ): T? {
        val responseObj = postObject(url, status, content)
        if (responseObj is JsonNull) return null
        val serializer = serializer(T::class.java)
        return Json.decodeFromJsonElement(serializer, responseObj) as T
    }

    internal fun post(url: String, status: ResultMatcher = MockMvcResultMatchers.status().isOk, content: () -> String) =
        mockMvc.perform(
            MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content())
        ).andExpect(status)
            .andReturn()
}