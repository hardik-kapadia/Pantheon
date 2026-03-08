package com.pantheon.backend.core.platform.dto;

import com.pantheon.backend.core.platform.model.ScanStrategy;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record PlatformLocalSetupDTO(
        ScanStrategy localScanStrategy,
        @NotNull String executablePath,
        String manifestsPath) {

    public PlatformLocalSetupDTO {

        if (localScanStrategy == null) localScanStrategy = ScanStrategy.LIBRARY;

        if (localScanStrategy == ScanStrategy.MANIFEST)
            if (manifestsPath == null)
                throw new IllegalArgumentException("Manifests path is required when scanning by manifests");

    }
}
