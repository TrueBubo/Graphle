package com.graphle.graphlemanager.connection

import com.graphle.graphlemanager.file.AbsolutePathString
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
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

    @MutationMapping
    fun addConnection(@Argument connection: ConnectionInput, @Argument bidirectional: Boolean): Connection {
        connectionService.addConnection(connection, bidirectional)
        return connection.run {
            Connection(
                name = name,
                value = value,
                from = locationFrom,
                to = locationTo,
                bidirectional = bidirectional
            )
        }
    }
}