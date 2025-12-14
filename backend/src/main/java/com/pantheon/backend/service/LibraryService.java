package com.pantheon.backend.service;

import com.pantheon.backend.client.LocalGameLibraryClient;
import com.pantheon.backend.dto.ScannedLocalGameDTO;
import com.pantheon.backend.mapper.GameMapper;
import com.pantheon.backend.model.Game;
import com.pantheon.backend.model.LibraryEntry;
import com.pantheon.backend.model.Platform;
import com.pantheon.backend.repository.GameRepository;
import com.pantheon.backend.repository.LibraryEntryRepository;
import com.pantheon.backend.repository.PlatformRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class LibraryService {

    private final PlatformRepository platformRepository;
    private final GameRepository gameRepository;
    private final LibraryEntryRepository libraryEntryRepository;
    private final GameMapper gameMapper;
    private final PlatformClientMapperService platformClientMapperService;
    private final LocalScanNotificationOrchestrationService localScanNotificationOrchestrationService;

    @Autowired

    public LibraryService(PlatformRepository platformRepository,
                          GameRepository gameRepository,
                          LibraryEntryRepository libraryEntryRepository,
                          PlatformClientMapperService platformClientMapperService,
                          GameMapper gameMapper,
                          LocalScanNotificationOrchestrationService localScanNotificationOrchestrationService) {

        this.platformRepository = platformRepository;
        this.gameRepository = gameRepository;
        this.libraryEntryRepository = libraryEntryRepository;
        this.gameMapper = gameMapper;
        this.platformClientMapperService = platformClientMapperService;
        this.localScanNotificationOrchestrationService = localScanNotificationOrchestrationService;

    }

    /**
     * @param platformName The name of the platform to scan
     * @throws IllegalArgumentException for incorrect platform name
     * @throws IllegalStateException    for correct platform name but no Scanner defined for it.
     */
    @Async
    @Transactional
    public void scanPlatform(String platformName) throws IllegalStateException, IllegalArgumentException {
        log.info("Requesting scan for platform {}", platformName);

        Platform platform = platformRepository.findByName(platformName)
                .orElseThrow(() -> {
                    log.error("Unknown Platform: {}", platformName);
                    return new IllegalArgumentException("Unknown platform: " + platformName);
                });

        LocalGameLibraryClient client = platformClientMapperService.getScanner(platform);

        String phase = "Initializing";

        List<String> libraryPaths = platform.getLibraryPaths();

        if (libraryPaths == null || libraryPaths.isEmpty()) {
            log.error("{}: No Library Paths Configured", platformName);
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

            } catch (Exception e) {
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


    private void processScannedGames(List<ScannedLocalGameDTO> scannedGames, Platform platform) {
        for (ScannedLocalGameDTO dto : scannedGames) {
            Game game = findOrCreateGame(dto);
            createOrUpdateLibraryEntry(game, platform, dto);
        }
        log.info("{}: Processed {} games ", platform.getName(), scannedGames.size());
    }

    private Game findOrCreateGame(ScannedLocalGameDTO dto) {

        return gameRepository.findByTitle(dto.title()).orElseGet(() -> {
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
