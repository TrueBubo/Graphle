package com.graphle.graphlemanager.File;

import com.graphle.graphlemanager.Tag.Tag;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileRepository extends Neo4jRepository<File, UUID> {
    @Query("MATCH (f:File {location: $fileLocation})-[:HasTag]->(t:Tag) RETURN t.name AS name, t.value AS value, t.numericValue AS numericValue")
    @NonNull List<Tag> tagsByFileLocation(@NonNull String fileLocation);
}
