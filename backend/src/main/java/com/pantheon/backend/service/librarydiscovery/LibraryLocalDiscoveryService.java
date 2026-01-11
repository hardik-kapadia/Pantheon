package com.pantheon.backend.service.librarydiscovery;

import com.pantheon.backend.model.Platform;
import com.pantheon.backend.repository.PlatformRepository;
import com.pantheon.backend.service.librarydiscovery.helper.PlatformLocalScanService;
import com.pantheon.backend.service.librarydiscovery.notification.LocalScanNotificationOrchestrationService;

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
public class LibraryLocalDiscoveryService {

    private final PlatformRepository platformRepository;
    private final LocalScanNotificationOrchestrationService localScanNotificationOrchestrationService;
    private final PlatformLocalScanService platformLocalScanService;

    @Autowired
    public LibraryLocalDiscoveryService(PlatformRepository platformRepository,
                                        PlatformLocalScanService platformLocalScanService,
                                        LocalScanNotificationOrchestrationService localScanNotificationOrchestrationService) {

        this.platformRepository = platformRepository;
        this.platformLocalScanService = platformLocalScanService;
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
                this.platformLocalScanService.scanPlatformPaths(platform);
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

        this.platformLocalScanService.scanPlatformPaths(platform);

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
