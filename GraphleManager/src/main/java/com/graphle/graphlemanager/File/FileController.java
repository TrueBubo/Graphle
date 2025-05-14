package com.graphle.graphlemanager.File;

import com.graphle.graphlemanager.Connections.RangedNeighborConnections;
import com.graphle.graphlemanager.Time.TimeRange;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.List;

@Controller
public class FileController {
    private final FileService fileService;
    public FileController(@NonNull FileService fileService) {
        this.fileService = fileService;
    }

    @QueryMapping
    public File fileByLocation(@Argument @NonNull String location) {
        return new File(location,
                Instant.now().toEpochMilli(),
                fileService.tagsForFileLocation(location),
                new RangedNeighborConnections(new TimeRange(0, Instant.now().getEpochSecond()), List.of()));
    }
}
