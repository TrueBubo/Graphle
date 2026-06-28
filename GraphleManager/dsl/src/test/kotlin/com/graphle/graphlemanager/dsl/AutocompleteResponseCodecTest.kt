package com.graphle.graphlemanager.dsl

import kotlin.test.Test
import kotlin.test.assertEquals

class AutocompleteResponseCodecTest {
    @Test
    fun `autocomplete response encodes empty list as empty frame`() {
        assertEquals("", encodeAutocompleteResponse(emptyList()))
    }

    @Test
    fun `autocomplete response preserves values with separators and escaped json characters`() {
        assertEquals(
            "8:/tmp:a,b15:quote\"and\\slash10:line\nbreak",
            encodeAutocompleteResponse(listOf("/tmp:a,b", "quote\"and\\slash", "line\nbreak"))
        )
    }

    @Test
    fun `autocomplete request decodes optional request id`() {
        assertEquals(AutocompleteRequest(id = 42, input = "detail /Users"), decodeAutocompleteRequest("42\tdetail /Users"))
        assertEquals(AutocompleteRequest(id = null, input = "detail /Users"), decodeAutocompleteRequest("detail /Users"))
    }

    @Test
    fun `autocomplete response preserves optional request id`() {
        assertEquals("42\t6:/tmp/a", encodeAutocompleteResponse(42, listOf("/tmp/a")))
        assertEquals("42\t", encodeAutocompleteResponse(42, emptyList()))
    }
}
