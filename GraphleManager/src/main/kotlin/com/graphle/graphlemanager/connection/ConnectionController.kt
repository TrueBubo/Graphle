package com.graphle.graphlemanager.connection

import com.graphle.graphlemanager.file.AbsolutePathString
import org.springframework.stereotype.Controller

/**
 * GraphQL controller for searching for relationships between files on the system
 * @param connectionService Service used for retrieving required information
 */
@Controller
class ConnectionController(val connectionService: ConnectionService) {
    /**
     * Retrieves the list of relations of fromLocation file together with all the files connected by said relationship
     * @param fromLocation Shows all the files connected to the file located at this location
     * @return List of neighbors
     */
    fun neighborsByFileLocation(fromLocation: AbsolutePathString): List<NeighborConnection> =
        connectionService.neighborsByFileLocation(fromLocation)
}