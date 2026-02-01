package com.pantheon.backend.core.platform.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pantheon.backend.core.platform.model.PlatformType;
import lombok.Builder;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlatformDTO(
        String name,
        String executablePath,
        List<String> libraryPaths,
        String iconUrl,
        PlatformType platformType) {
}
