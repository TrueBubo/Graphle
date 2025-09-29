package com.graphle.graphlemanager.tag

import com.graphle.graphlemanager.file.AbsolutePathString
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Neo4J repository for managing information related to tags
 */
@Repository
interface TagRepository : Neo4jRepository<Tag, UUID> {
    /**
     * Retrieves the list of all the tags for a file at a particular location
     * @param fileLocation Absolute path of the file in server filesystem
     * @return Tags corresponding to the [fileLocation]
     */
    @Query("MATCH (f:File {location: \$fileLocation})-[:HasTag]->(t:Tag) RETURN t.name AS _name, t.value AS value")
    fun tagsByFileLocation(fileLocation: AbsolutePathString): List<Tag>

    /**
     * Marks the given file with a tag via a connection to a tag node
     * @param fileLocation Absolute path of the file in server filesystem
     * @param tagName Tag the system associate will with the file
     * @param tagValue Value of the [tagName] property
     * @return Inserted tag
     */
    @Query("MERGE (f:File {location: \$fileLocation}) MERGE (t:Tag {name: \$tagName, value: \$tagValue}) MERGE (f)-[:HasTag]->(t)")
    fun addTagToFile(fileLocation: AbsolutePathString, tagName: String, tagValue: String)

    /**
     * Marks the given file with a tag via a connection to a tag node
     * @param fileLocation Absolute path of the file in server filesystem
     * @param tagName Tag the system associate will with the file
     * @return Inserted tag
     */
    @Query("MERGE (f:File {location: \$fileLocation}) MERGE (t:Tag {name: \$tagName}) MERGE (f)-[:HasTag]->(t)")
    fun addTagToFile(fileLocation: AbsolutePathString, tagName: String)

    /**
     * Retrieves the absolute paths of all the files containing the given tag
     * @param tagName name of tag to search for
     * @return Absolute paths of files with tag with the given name
     */
    @Query("MATCH (file:File)-[:HasTag]-(tag:Tag {name: \$tagName}) return file.location as location, tag.name as tagName, tag.value as tagValue")
    fun filesByTag(tagName: String): List<TagForFileFlattened>

    /**
     * Removes tags which are no longer connected to any file
     */
    @Query("MATCH (tag:Tag) WHERE NOT (tag)--() DELETE tag")
    fun removeOrphanTags()
}
