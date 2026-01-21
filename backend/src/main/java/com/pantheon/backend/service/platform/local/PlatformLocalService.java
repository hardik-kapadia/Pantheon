package com.pantheon.backend.service.platform.local;

import com.pantheon.backend.dto.platform.PlatformDTO;
import com.pantheon.backend.dto.platform.PlatformSetupDTO;
import com.pantheon.backend.model.Platform;
import com.pantheon.backend.model.PlatformType;
import com.pantheon.backend.repository.PlatformRepository;
import com.pantheon.backend.service.utils.ScannerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlatformLocalService {

    private final PlatformRepository platformRepository;
    private final ScannerUtil scannerUtil;

    public List<Platform> getAllPlatforms() {
        return platformRepository.findAll();
    }

    public Platform getPlatformByName(String name) throws IllegalArgumentException {
        return platformRepository.findByName(name).orElseThrow(() -> new IllegalArgumentException("Platform with name " + name + " not found"));
    }

    public PlatformDTO setupLocalPlatform(PlatformSetupDTO platformSetupDTO) throws IllegalArgumentException {

        Platform platform = getPlatformByName(platformSetupDTO.name());
        platform.setExecutablePath(platformSetupDTO.executablePath());

        platform.setType(PlatformType.API);

        if (platformSetupDTO.iconUrl() != null) {
            platform.setIconUrl(platformSetupDTO.iconUrl());
        }

        if (platformSetupDTO.libraryPaths() != null && !platformSetupDTO.libraryPaths().isEmpty()) {
            platform.getLibraryPaths().clear();
            platform.getLibraryPaths().addAll(platformSetupDTO.libraryPaths());
        }

        Platform saved = platformRepository.save(platform);

        scannerUtil.getScannerForPlatform(saved).refreshPlatform();

        return platformDTO(saved);
    }

    private PlatformDTO platformDTO(Platform platform) {
        return PlatformDTO.builder()
                .name(platform.getName())
                .executablePath(platform.getExecutablePath())
                .libraryPaths(platform.getLibraryPaths())
                .platformType(platform.getType())
                .iconUrl(platform.getIconUrl())
                .build();
    }

}
