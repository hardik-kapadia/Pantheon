package com.pantheon.backend.service.librarydiscovery.helper;

import com.pantheon.backend.core.localscanner.LocalGameLibraryScanner;
import com.pantheon.backend.dto.ScannedLocalGameDTO;
import com.pantheon.backend.exception.ScanFailureException;
import com.pantheon.backend.mapper.GameMapper;
import com.pantheon.backend.model.Game;
import com.pantheon.backend.model.LibraryEntry;
import com.pantheon.backend.model.Platform;
import com.pantheon.backend.repository.GameRepository;
import com.pantheon.backend.repository.LibraryEntryRepository;
import com.pantheon.backend.service.librarydiscovery.notification.LocalScanNotificationOrchestrationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PlatformLocalScanService {

    private final GameRepository gameRepository;
    private final LibraryEntryRepository libraryEntryRepository;
    private final GameMapper gameMapper;
    private final PlatformClientMapperService platformClientMapperService;
    private final LocalScanNotificationOrchestrationService localScanNotificationOrchestrationService;

    @Autowired
    public PlatformLocalScanService(GameRepository gameRepository,
                                    LibraryEntryRepository libraryEntryRepository,
                                    PlatformClientMapperService platformClientMapperService,
                                    GameMapper gameMapper,
                                    LocalScanNotificationOrchestrationService localScanNotificationOrchestrationService) {

        this.gameRepository = gameRepository;
        this.libraryEntryRepository = libraryEntryRepository;
        this.gameMapper = gameMapper;
        this.platformClientMapperService = platformClientMapperService;
        this.localScanNotificationOrchestrationService = localScanNotificationOrchestrationService;

    }

    @Async
    public void scanPlatformPaths(Platform platform) throws IllegalStateException {

        String platformName = platform.getName();
        LocalGameLibraryScanner client;
        try {
            client = platformClientMapperService.getScanner(platform);
        } catch (IllegalStateException e) {
            localScanNotificationOrchestrationService.notifyError(platformName, e.getMessage());
            throw e;
        }

        String phase = "Initializing";

        List<String> libraryPaths = platform.getLibraryPaths();

        if (libraryPaths == null || libraryPaths.isEmpty()) {
            log.error("{}: No Library Paths Configured", platformName);
            localScanNotificationOrchestrationService.notifyError(platformName, "No Library Paths Configured");
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

                processScannedGames(foundGames, platform);

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

    @Transactional
    protected void processScannedGames(List<ScannedLocalGameDTO> scannedGames, Platform platform) {
        for (ScannedLocalGameDTO dto : scannedGames) {
            Game game = findOrCreateGame(dto);
            createOrUpdateLibraryEntry(game, platform, dto);
        }
        log.info("{}: Processed {} games ", platform.getName(), scannedGames.size());
    }

    private Game findOrCreateGame(ScannedLocalGameDTO dto) {

        return gameRepository.findByTitle(dto.title()).orElseGet(
                () -> {
                    Game newGame = gameMapper.toEntity(dto);
                    return gameRepository.save(newGame);
                });
    }

    private void createOrUpdateLibraryEntry(Game game, Platform platform, ScannedLocalGameDTO dto) {

        LibraryEntry entry = libraryEntryRepository.findByGameIdAndPlatformId(game.getId(), platform.getId())
                .orElseGet(() -> {
                    LibraryEntry newEntry = new LibraryEntry();
                    newEntry.setGame(game);
                    newEntry.setPlatform(platform);
                    return newEntry;
                });

        entry.setInstalled(dto.isInstalled());
        entry.setInstallPath(dto.installPath());
        entry.setPlatformGameId(dto.platformGameId());

        if (dto.playtimeMinutes() != null) {
            entry.setPlaytimeMinutes(dto.playtimeMinutes());
        }
        if (dto.lastPlayed() != null) {
            entry.setLastPlayed(dto.lastPlayed());
        }

        libraryEntryRepository.save(entry);
    }
}