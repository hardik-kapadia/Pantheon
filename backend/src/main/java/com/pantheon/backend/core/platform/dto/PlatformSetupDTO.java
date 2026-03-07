package com.pantheon.backend.core.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record PlatformSetupDTO(
        @NotBlank(message = "Platform Name is required") String name,
        String iconUrl,
        String scanStrategy,
        String executablePath,
        String manifestsPath) {

    public PlatformSetupDTO {

        if (scanStrategy == null) scanStrategy = "LIBRARY";

    }
}
