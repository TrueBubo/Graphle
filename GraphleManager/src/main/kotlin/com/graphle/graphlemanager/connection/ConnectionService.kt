package com.graphle.graphlemanager.connection

import org.springframework.stereotype.Service

/**
 * Service for searching for relationships between files on the system
 * @param connectionRepository Repository used for retrieving required information
 */
@Service
class ConnectionService(val connectionRepository: ConnectionRepository) {
    /**
     * Retrieves the list of relations of fromLocation file together with all the files connected by said relationship
     * @param fromLocation Retrieves all the files connected to the file located at this location
     * @return List of neighbors
     */
    fun neighborsByFileLocation(fromLocation: String): List<NeighborConnection> {
        return listOf()
    }
}
