package com.graphle.graphlemanager.File;

import com.graphle.graphlemanager.Tag.Tag;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileService {
    private final FileRepository fileRepository;
    public FileService(@NonNull FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public @NonNull List<Tag> tagsForFileLocation(@NonNull String location) {
        return fileRepository.tagsByFileLocation(location);
    }
}
