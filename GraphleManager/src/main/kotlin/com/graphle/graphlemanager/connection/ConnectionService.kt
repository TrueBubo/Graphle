package com.graphle.graphlemanager.connection

import org.springframework.stereotype.Service

@Service
class ConnectionService(val connectionRepository: ConnectionRepository) {
    fun neighborsByFileLocation(location: String): List<NeighborConnection> {
        return listOf()
    }

}
