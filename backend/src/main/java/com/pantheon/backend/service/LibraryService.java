package com.pantheon.backend.service;

import com.pantheon.backend.client.LocalGameLibraryClient;
import com.pantheon.backend.dto.ScannedLocalGameDTO;
import com.pantheon.backend.model.Game;
import com.pantheon.backend.model.LibraryEntry;
import com.pantheon.backend.model.Platform;
import com.pantheon.backend.repositories.GameRepository;
import com.pantheon.backend.repositories.LibraryEntryRepository;
import com.pantheon.backend.repositories.PlatformRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LibraryService {

    private final PlatformRepository platformRepository;
    private final GameRepository gameRepository;
    private final LibraryEntryRepository libraryEntryRepository;
    private final Map<String, LocalGameLibraryClient> scannerMap;

    @Autowired
    public LibraryService(PlatformRepository platformRepository, GameRepository gameRepository, LibraryEntryRepository libraryEntryRepository, List<LocalGameLibraryClient> scanners) {
        this.platformRepository = platformRepository;
        this.gameRepository = gameRepository;
        this.libraryEntryRepository = libraryEntryRepository;

        // Convert the List to a Map for fast lookup
        this.scannerMap = scanners.stream().collect(Collectors.toMap(LocalGameLibraryClient::getPlatformName, Function.identity()));
    }


    @Transactional
    public void scanPlatform(String platformName) {

        // 1. Find the Platform Config (e.g. "Steam") in DB
        Platform platform = platformRepository.findByName(platformName).orElseThrow(() -> new IllegalArgumentException("Unknown platform: " + platformName));

        // 2. Find the correct Scanner Worker
        LocalGameLibraryClient scanner = scannerMap.get(platform.getName());

        if (scanner == null) {
            throw new IllegalStateException("No scanner implementation found for type: " + platform.getName());
        }

        log.info("Starting scan for {} ...", platformName);

        for (String pathStr : platform.getLibraryPaths()) {
            List<ScannedLocalGameDTO> foundGames = scanner.scan(Path.of(pathStr));

            processScannedGames(foundGames, platform);
        }
    }

    private void processScannedGames(List<ScannedLocalGameDTO> scannedGames, Platform platform) {
        for (ScannedLocalGameDTO dto : scannedGames) {
            Game game = findOrCreateGame(dto);
            createOrUpdateLibraryEntry(game, platform, dto);
        }
        log.info("Processed {} games ", scannedGames.size());
    }

    private Game findOrCreateGame(ScannedLocalGameDTO dto) {

        return gameRepository.findByTitle(dto.getTitle()).orElseGet(() -> {
            Game newGame = new Game();
            newGame.setTitle(dto.getTitle());
            return gameRepository.save(newGame);
        });
    }

    private void createOrUpdateLibraryEntry(Game game, Platform platform, ScannedLocalGameDTO dto) {

        LibraryEntry entry = libraryEntryRepository.findByGameIdAndPlatformId(game.getId(), platform.getId()).orElseGet(() -> {
            LibraryEntry newEntry = new LibraryEntry();
            newEntry.setGame(game);
            newEntry.setPlatform(platform);
            return newEntry;
        });

        entry.setInstalled(dto.isInstalled());
        entry.setInstallPath(dto.getInstallPath());
        entry.setPlatformGameId(dto.getPlatformGameId());

        if (dto.getPlaytimeMinutes() != null) {
            entry.setPlaytimeMinutes(dto.getPlaytimeMinutes());
        }
        if (dto.getLastPlayed() != null) {
            entry.setLastPlayed(dto.getLastPlayed());
        }

        libraryEntryRepository.save(entry);
    }

}
