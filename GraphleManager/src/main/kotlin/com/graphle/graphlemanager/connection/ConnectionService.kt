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
        val bidirectional = connection.bidirectional
        when {
            (value == null) && bidirectional -> {
                connectionRepository.addConnection(name, locationFrom, locationTo)
                connectionRepository.addConnection(name, locationTo, locationFrom)
            }

            (value != null) && bidirectional -> {
                connectionRepository.addConnection(name, value, locationFrom, locationTo)
                connectionRepository.addConnection(name, value, locationTo, locationFrom)
            }

            (value == null) && !bidirectional ->
                connectionRepository.addConnection(name, locationFrom, locationTo)

            (value != null) && !bidirectional -> connectionRepository.addConnection(name, value, locationFrom, locationTo)
        }
    }
}

