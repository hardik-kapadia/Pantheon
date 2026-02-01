package com.pantheon.backend.core.inventory.local;

import com.pantheon.backend.model.Platform;
import com.pantheon.backend.repository.PlatformRepository;
import com.pantheon.backend.core.notification.LocalScanNotificationOrchestrationService;
import com.pantheon.backend.core.inventory.local.processor.InventoryLocalScanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Service responsible for orchestrating local library discovery tasks.
 * <p>
 * This service acts as the entry point for scanning operations, resolving platform names to {@link Platform} entities
 * and dispatching scan requests to the processing layer.
 * All public scan methods are execution-heavy and run asynchronously.
 * </p>
 */
@Slf4j
@Service
public class InventoryLocalDiscoveryService {

    private final PlatformRepository platformRepository;
    private final LocalScanNotificationOrchestrationService localScanNotificationOrchestrationService;
    private final InventoryLocalScanService inventoryLocalScanService;

    @Autowired
    public InventoryLocalDiscoveryService(PlatformRepository platformRepository,
                                          InventoryLocalScanService inventoryLocalScanService,
                                          LocalScanNotificationOrchestrationService localScanNotificationOrchestrationService) {

        this.platformRepository = platformRepository;
        this.inventoryLocalScanService = inventoryLocalScanService;
        this.localScanNotificationOrchestrationService = localScanNotificationOrchestrationService;

    }

    /**
     * Scans all platforms
     *
     * @throws IllegalArgumentException for incorrect platform name
     * @throws IllegalStateException    for correct platform name but no Scanner defined for it.
     */
    @Async
    public void scanPlatforms() throws IllegalStateException, IllegalArgumentException {
        scanPlatforms(null);
    }

    /**
     * Scans provided platforms
     *
     * @param platforms platforms to scan
     * @throws IllegalArgumentException for incorrect platform name
     * @throws IllegalStateException    for correct platform name but no Scanner defined for it.
     */
    @Async
    public void scanPlatforms(String[] platforms) throws IllegalStateException, IllegalArgumentException {

        List<Platform> platformList;

        if (platforms == null || platforms.length == 0) {
            log.info("Attempting scan request initiation for all platforms");
            platformList = platformRepository.findAll();
        } else {
            log.info("Attempting scan request initiation for platforms: {}", Arrays.toString(platforms));
            platformList = Arrays.stream(platforms).map(platformName -> {
                try {
                    return getPlatformByName(platformName);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }).filter(Objects::nonNull).toList();
        }

        log.info("Initiating Scan request for: {}", platformList);

        for (Platform platform : platformList) {
            try {
                this.inventoryLocalScanService.scanPlatformPaths(platform);
            } catch (IllegalStateException | IllegalArgumentException e) {
                log.error("{}: Error while scanning platform {}", platform.getName(), e.getMessage());
            }
        }

    }

    /**
     * Scans the specified platform
     *
     * @param platformName The name of the platform to scan
     * @throws IllegalArgumentException for incorrect platform name
     * @throws IllegalStateException    for correct platform name but no Scanner defined for it.
     */
    @Async
    public void scanPlatform(String platformName) throws IllegalStateException, IllegalArgumentException {
        log.info("Requesting scan for platform {}", platformName);

        Platform platform = getPlatformByName(platformName);

        this.inventoryLocalScanService.scanPlatformPaths(platform);

    }

    /**
     * @param platformName Platform Name
     * @return the Platform Object
     * @throws IllegalArgumentException when Platform is not found
     */
    private Platform getPlatformByName(String platformName) throws IllegalArgumentException {
        return platformRepository.findByName(platformName)
                .orElseThrow(() -> {
                    log.error("{}: Unknown Platform", platformName);
                    localScanNotificationOrchestrationService.notifyError(platformName, "Platform not found");
                    return new IllegalArgumentException("Unknown platform: " + platformName);
                });
    }

}
