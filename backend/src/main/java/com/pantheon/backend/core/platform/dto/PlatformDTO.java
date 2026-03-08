package com.pantheon.backend.core.platform.dto;

import lombok.Builder;

@Builder
public record PlatformDTO(
        Integer id,
        String name,
        String iconUrl,
        PlatformLocalSetupDTO localSetup
) {
}
