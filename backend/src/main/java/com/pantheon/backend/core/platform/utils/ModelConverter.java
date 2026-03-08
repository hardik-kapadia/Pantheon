package com.pantheon.backend.core.platform.utils;

import com.pantheon.backend.core.platform.dto.PlatformDTO;
import com.pantheon.backend.core.platform.dto.PlatformLocalSetupDTO;
import com.pantheon.backend.core.platform.model.Platform;

public class ModelConverter {

    public static PlatformDTO mapPlatformToDTO(Platform platform) {
        var builder = PlatformDTO.builder()
                .name(platform.getName())
                .iconUrl(platform.getIconUrl());

        var localConfig = platform.getPlatformLocalConfig();

        if (localConfig != null) {
            var localSetupDTOBuilder = PlatformLocalSetupDTO.builder()
                    .localScanStrategy(localConfig.getLocalScanStrategy())
                    .executablePath(localConfig.getExecutablePath());

            if (localConfig.getManifestsPath() != null)
                localSetupDTOBuilder.manifestsPath(localConfig.getManifestsPath());

            builder.localSetup(localSetupDTOBuilder.build());
        }

        return builder.build();
    }

}
