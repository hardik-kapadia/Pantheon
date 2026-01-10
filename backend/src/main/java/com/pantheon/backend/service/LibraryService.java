package com.pantheon.backend.service;

import com.pantheon.backend.model.Platform;
import com.pantheon.backend.repository.PlatformRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Transactional
public class LibraryService {

    private final PlatformRepository platformRepository;
    private final LocalScanNotificationOrchestrationService localScanNotificationOrchestrationService;
    private final LibraryScanService libraryScanService;

    @Autowired
    public LibraryService(PlatformRepository platformRepository,
                          LibraryScanService libraryScanService,
                          LocalScanNotificationOrchestrationService localScanNotificationOrchestrationService) {

        this.platformRepository = platformRepository;
        this.libraryScanService = libraryScanService;
        this.localScanNotificationOrchestrationService = localScanNotificationOrchestrationService;

    }

    @Async
    public void scanPlatforms() throws IllegalStateException, IllegalArgumentException {
        scanPlatforms(null);
    }

    @Async
    public void scanPlatforms(String[] platforms) throws IllegalStateException, IllegalArgumentException {

        List<Platform> platformList;

        if (platforms == null || platforms.length == 0) {
            platformList = platformRepository.findAll();
        } else {
            platformList = Arrays.stream(platforms).map(platformName -> {
                try {
                    return getPlatformByName(platformName);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }).filter(Objects::nonNull).toList();
        }

        for (Platform platform : platformList) {
            try {
                this.libraryScanService.scanPlatformPaths(platform);
            } catch (IllegalStateException | IllegalArgumentException e) {
                log.error("{}: Error while scanning platform {}", platform.getName(), e.getMessage());
            }
        }

    }

    /**
     * @param platformName The name of the platform to scan
     * @throws IllegalArgumentException for incorrect platform name
     * @throws IllegalStateException    for correct platform name but no Scanner defined for it.
     */
    @Async
    public void scanPlatform(String platformName) throws IllegalStateException, IllegalArgumentException {
        log.info("Requesting scan for platform {}", platformName);

        Platform platform = getPlatformByName(platformName);

        this.libraryScanService.scanPlatformPaths(platform);

    }

    private Platform getPlatformByName(String platformName) throws IllegalArgumentException {
        return platformRepository.findByName(platformName)
                .orElseThrow(() -> {
                    log.error("{}: Unknown Platform", platformName);
                    localScanNotificationOrchestrationService.notifyError(platformName, "Platform not found");
                    return new IllegalArgumentException("Unknown platform: " + platformName);
                });
    }

}
