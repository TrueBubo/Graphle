package com.graphle.graphlemanager.connection

import BaseIntegrationTest
import kotlin.test.Test

class ConnectionIntegrationTests : BaseIntegrationTest() {
    @Test
    fun `insert connection`() {
        post { insertConnectionQuery }
    }

    private val insertConnectionQuery = mutation {
        addConnection(
            ConnectionInput(
                name = "exampleConnection",
                value = "exampleValue",
                locationFrom = "/testFrom",
                locationTo = "/testTo",
                bidirectional = true
            )
        )
    }

    companion object {
        private fun addConnection(connection: ConnectionInput) = """
          addConnection(connection: {
                name: "${connection.name}",
                ${if (connection.value == null) "" else "value: \"${connection.value}\","} 
                locationFrom: "${connection.locationFrom}",
                locationTo: "${connection.locationTo}",
                bidirectional: ${connection.bidirectional}
          }) {
            name
            value
            locationFrom
            locationTo
            bidirectional
          }
        """.trimIndent()
    }
}