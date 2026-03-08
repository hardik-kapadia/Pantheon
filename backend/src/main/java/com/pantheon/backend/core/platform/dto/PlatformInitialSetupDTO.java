package com.pantheon.backend.core.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record PlatformInitialSetupDTO(
        @NotBlank(message = "Platform Name is required") String name,
        String iconUrl,
        PlatformLocalSetupDTO localSetup) {
}
