package com.graphle.graphlemanager.connection

import com.graphle.graphlemanager.commons.normalize
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
     * @param locationFrom Shows all the files connected to the file located at this location
     * @return List of neighbors
     */
    fun neighborsByFileLocation(locationFrom: AbsolutePathString): List<NeighborConnection> =
        connectionService.neighborsByFileLocation(locationFrom.normalize())

    /**
     * Adds a new connection between two files
     * @param connection Input data containing connection details
     * @return The created connection
     */
    @MutationMapping
    fun addConnection(@Argument connection: ConnectionInput): Connection {
        connectionService.addConnection(
            connection.copy(
                from = connection.from.normalize(),
                to = connection.to.normalize())
        )
        return connection.run {
            Connection(
                name = name,
                value = value,
                from = from.normalize(),
                to = to.normalize(),
                bidirectional = bidirectional
            )
        }
    }

    /**
     * Removes an existing connection between two files
     * @param connection Input data containing connection details to remove
     * @return The removed connection
     */
    @MutationMapping
    fun removeConnection(@Argument connection: ConnectionInput): Connection {
        connectionService.removeConnection(
            connection.copy(
                from = connection.from.normalize(),
                to = connection.to.normalize()
            )
        )
        return connection.run {
            Connection(
                name = name,
                value = value,
                from = from.normalize(),
                to = to.normalize(),
                bidirectional = bidirectional
            )
        }
    }
}