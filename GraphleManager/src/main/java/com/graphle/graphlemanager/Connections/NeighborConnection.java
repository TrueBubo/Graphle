package com.graphle.graphlemanager.Connections;

import com.graphle.graphlemanager.File.File;
import org.springframework.lang.NonNull;

import java.util.List;

public record NeighborConnection(@NonNull String relationship, @NonNull List<File> toFiles) {
}
