package com.graphle.graphlemanager.connection

import com.graphle.graphlemanager.commons.AbsolutePathString
import org.springframework.stereotype.Service

/**
 * Service for searching for relationships between files on the system
 * @param connectionRepository Repository used for retrieving required information
 */
@Service
class ConnectionService(val connectionRepository: ConnectionRepository) {
    /**
     * Retrieves the list of relations of fromLocation file together with all the files connected by said relationship
     * @param locationFrom Retrieves all the files connected to the file located at this location
     * @return List of neighbors
     */
    fun neighborsByFileLocation(locationFrom: AbsolutePathString): List<NeighborConnection> {
        return connectionRepository.neighborsByFileLocation(locationFrom)
    }

    /**
     * Adds a connection between files, handling bidirectional relationships
     * @param connection Connection input data specifying the relationship to create
     */
    fun addConnection(connection: ConnectionInput) = with(connection) {
        val relationshipValue = value
        when {
            (relationshipValue == null) && bidirectional -> {
                connectionRepository.addConnection(name, from, to)
                connectionRepository.addConnection(name, to, from)
            }

            (relationshipValue != null) && bidirectional -> {
                connectionRepository.addConnection(name, relationshipValue, from, to)
                connectionRepository.addConnection(name, relationshipValue, to, from)
            }

            (relationshipValue == null) && !bidirectional ->
                connectionRepository.addConnection(name, from, to)

            (relationshipValue != null) && !bidirectional -> connectionRepository.addConnection(
                name,
                relationshipValue,
                from,
                to
            )
        }
    }

    /**
     * Removes a connection between files, handling bidirectional relationships
     * @param connection Connection input data specifying the relationship to remove
     */
    fun removeConnection(connection: ConnectionInput) = with(connection) {
        val relationshipValue = value
        if (relationshipValue == null) {
            connectionRepository.removeConnection(from, to, name)
            if (bidirectional) connectionRepository.removeConnection(from, to, name)
        } else {
            connectionRepository.removeConnection(from, to, name, relationshipValue)
            if (bidirectional) connectionRepository.removeConnection(to, from, name, relationshipValue)
        }
    }
}
