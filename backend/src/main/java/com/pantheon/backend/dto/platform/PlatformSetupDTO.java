package com.pantheon.backend.dto.platform;

import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder
public record PlatformSetupDTO(
        String name,
        String executablePath,
        List<String> libraryPaths,
        String iconUrl) {

    public PlatformSetupDTO {
        if (libraryPaths == null) {
            libraryPaths = new ArrayList<>();
        }
    }
}
