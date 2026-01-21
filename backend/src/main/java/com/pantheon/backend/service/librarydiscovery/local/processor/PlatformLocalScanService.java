package com.pantheon.backend.service.librarydiscovery.local.processor;

import com.pantheon.backend.core.localscanner.LocalGameLibraryScanner;
import com.pantheon.backend.dto.ScannedLocalGameDTO;
import com.pantheon.backend.exception.ScanFailureException;
import com.pantheon.backend.model.Platform;
import com.pantheon.backend.service.librarydiscovery.local.notification.LocalScanNotificationOrchestrationService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * Service Responsible for initiating the path-specific scans, notifications and further processing
 *
 * <p>
 * This service is responsible for orchestrating the scans, the notifications related to a scan as well as the
 * post-processing for scanned games
 * </p>
 *
 */
@Slf4j
@Service
public class PlatformLocalScanService {

    private final LocalScanNotificationOrchestrationService localScanNotificationOrchestrationService;
    private final Map<String, LocalGameLibraryScanner> localGameLibraryClientMap;
    private final PlatformLocalGamesProcessor platformLocalGamesProcessor;

    /**
     * Initializes the scan service and builds a strategy map of available scanners.
     *
     * @param localScanNotificationOrchestrationService Orchestrator for scan-progress events.
     * @param platformLocalGamesProcessor               Responsible for the actual path-specific scanning
     * @param localGameLibraryScanners                  List of all {@link LocalGameLibraryScanner} beans found in the context.
     */
    @Autowired
    public PlatformLocalScanService(LocalScanNotificationOrchestrationService localScanNotificationOrchestrationService,
                                    List<LocalGameLibraryScanner> localGameLibraryScanners,
                                    PlatformLocalGamesProcessor platformLocalGamesProcessor) {

        this.localScanNotificationOrchestrationService = localScanNotificationOrchestrationService;
        this.localGameLibraryClientMap = localGameLibraryScanners.stream()
                .collect(Collectors.toMap(LocalGameLibraryScanner::getPlatformName, Function.identity()));

        this.platformLocalGamesProcessor = platformLocalGamesProcessor;
    }

    /**
     *
     * Responsible for initiating the path-specific scans via {@link LocalGameLibraryScanner}, initiating notifications
     * via {@link LocalScanNotificationOrchestrationService} as well as initiating the processing for the scanned games
     * via {@link PlatformLocalGamesProcessor}
     *
     * @param platform The Platform for which we need to scan games
     * @throws IllegalStateException when there's no scanner configured for the platform
     */
    @Async
    public void scanPlatformPaths(Platform platform) throws IllegalStateException {

        log.info("{}: Attempting scan", platform.getName());

        String platformName = platform.getName();
        LocalGameLibraryScanner client;

        try {
            client = getScannerForPlatform(platform);
        } catch (IllegalStateException e) {
            localScanNotificationOrchestrationService.notifyError(platformName, e.getMessage());
            throw e;
        }

        String phase = "Initializing";

        List<String> libraryPaths = client.getConfiguredLibraryPaths();

        if (libraryPaths == null || libraryPaths.isEmpty()) {
            log.error("{}: No Library Paths Configured", platform.getName());
            localScanNotificationOrchestrationService.notifyError(platform.getName(), "No Library Paths Configured");
            throw new IllegalStateException("No libraries paths configured");
        }

        localScanNotificationOrchestrationService.notifyStart(platformName);

        List<String> failedPaths = new ArrayList<>();

        int totalGamesFound = 0;

        for (String pathStr : libraryPaths) {

            log.info("{}: Initializing scan for path: {}", platformName, pathStr);

            try {

                phase = "Scanning";

                List<ScannedLocalGameDTO> foundGames = client.scan(Path.of(pathStr));

                phase = "Processing";

                this.platformLocalGamesProcessor.processScannedGames(foundGames, platform);

                phase = "Sending";

                localScanNotificationOrchestrationService.notifyBatch(platformName, foundGames);

                totalGamesFound += foundGames.size();

                log.info("{}: Scan Succeeded for path {}", platformName, pathStr);

            } catch (ScanFailureException e) {
                log.error("{}: Scan failed for path {} in phase {} with exception: {}", platformName, pathStr, phase, e.getMessage(), e);
                failedPaths.add(pathStr);
            }
        }

        if (failedPaths.size() == platform.getLibraryPaths().size()) {
            log.error("{}: Scan failed for all paths", platformName);
            localScanNotificationOrchestrationService.notifyError(platformName, failedPaths.size(), failedPaths);
        } else if (failedPaths.isEmpty()) {
            log.info("{}: Scan completed, totalPaths: {}, all succeeded", platformName, libraryPaths.size());
            localScanNotificationOrchestrationService.notifyComplete(platformName, totalGamesFound);
        } else {
            log.info("{}: Scan completed, totalPaths: {}, failed: {}", platformName, libraryPaths.size(), failedPaths);
            localScanNotificationOrchestrationService.notifyComplete(platformName, totalGamesFound, failedPaths.size(), failedPaths);
        }
    }

    private LocalGameLibraryScanner getScannerForPlatform(@NonNull Platform platform) throws IllegalStateException {

        LocalGameLibraryScanner scanner = localGameLibraryClientMap.get(platform.getName());

        if (scanner == null) {
            log.error("{}: No LibraryClient found ", platform.getName());
            throw new IllegalStateException("No scanner implementation found for type: " + platform.getName());
        }

        return scanner;
    }

}