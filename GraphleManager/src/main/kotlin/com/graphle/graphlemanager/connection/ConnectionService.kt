package com.graphle.graphlemanager.connection

import com.graphle.graphlemanager.file.AbsolutePathString
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

    fun addConnection(connection: ConnectionInput) = with(connection) {
        when {
            (value == null) && bidirectional -> {
                connectionRepository.addConnection(name, from, to)
                connectionRepository.addConnection(name, to, from)
            }

            (value != null) && bidirectional -> {
                connectionRepository.addConnection(name, value, from, to)
                connectionRepository.addConnection(name, value, to, from)
            }

            (value == null) && !bidirectional ->
                connectionRepository.addConnection(name, from, to)

            (value != null) && !bidirectional -> connectionRepository.addConnection(
                name,
                value,
                from,
                to
            )
        }
    }

    fun removeConnection(connection: ConnectionInput) = with(connection) {
        System.err.println(connection)
        if (value == null) {
            connectionRepository.removeConnection(from, to, name)
            if (bidirectional) connectionRepository.removeConnection(from, to, name)
        } else {
            connectionRepository.removeConnection(from, to, name, value)
            if (bidirectional) connectionRepository.removeConnection(to, from, name, value)
        }
    }
}

