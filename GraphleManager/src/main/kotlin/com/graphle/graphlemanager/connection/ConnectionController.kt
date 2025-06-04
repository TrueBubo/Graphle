package com.graphle.graphlemanager.connection

import org.springframework.stereotype.Controller

@Controller
class ConnectionController(val connectionService: ConnectionService) {
    fun neighborsByFileLocation(location: String): List<NeighborConnection> =
        connectionService.neighborsByFileLocation(location)
}