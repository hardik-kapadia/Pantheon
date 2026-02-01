package com.pantheon.backend.external.scanner.local;

import com.pantheon.backend.core.library.local.LocalGameLibraryScanner;
import com.pantheon.backend.core.inventory.local.dto.ScannedLocalGameDTO;
import com.pantheon.backend.core.library.exception.ScanFailureException;
import com.pantheon.backend.core.platform.model.PlatformType;
import com.pantheon.backend.core.platform.repository.PlatformRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
@Component
class LocalSteamLibraryScanner extends LocalGameLibraryScanner {

    private static final String STEAMAPPS_DIR = "steamapps";
    private static final String COMMON_DIR = "common";
    private static final String MANIFEST_PREFIX = "appmanifest_";
    private static final String MANIFEST_EXT = ".acf";

    private static final Pattern VDF_PAIR_PATTERN = Pattern.compile("\"([^\"]+)\"\\s+\"([^\"]+)\"");

    protected LocalSteamLibraryScanner(PlatformRepository platformRepository) {
        super(platformRepository);
    }

    @Override
    public String getPlatformName() {
        return "Steam";
    }

    @Override
    public List<ScannedLocalGameDTO> scan(Path libraryPath) throws ScanFailureException {
        log.info("🔎 Steam Scanner: Scanning directory {}", libraryPath);

        Path steamAppsPath = libraryPath.resolve(STEAMAPPS_DIR);
        if (!Files.exists(steamAppsPath) || !Files.isDirectory(steamAppsPath)) {
            log.warn("Steam library path valid, but '{}' directory not found at {}", STEAMAPPS_DIR, libraryPath);
            return List.of();
        }

        List<ScannedLocalGameDTO> foundGames = new ArrayList<>();

        try (Stream<Path> files = Files.list(steamAppsPath)) {
            List<Path> manifests = files
                    .filter(path -> {
                        String fileName = path.getFileName().toString();
                        return fileName.startsWith(MANIFEST_PREFIX) && fileName.endsWith(MANIFEST_EXT);
                    })
                    .toList();

            log.info("Found {} manifest files in {}", manifests.size(), steamAppsPath);

            // 3. Process each manifest
            for (Path manifest : manifests) {
                try {
                    ScannedLocalGameDTO game = parseManifest(manifest, libraryPath);

                    if (game != null) foundGames.add(game);

                } catch (IOException ioe) {
                    log.error("Failed to parse Steam manifest: {}. Error: {}", manifest.getFileName(), ioe.getMessage());
                }
            }

        } catch (IOException e) {
            throw new ScanFailureException("Failed to list files in Steam library: " + e.getMessage(), e);
        }

        return foundGames;
    }

    private ScannedLocalGameDTO parseManifest(Path manifestPath, Path rootLibraryPath) throws IOException {

        log.info("Parsing Steam manifest file {}", manifestPath);

        List<String> lines = Files.readAllLines(manifestPath);
        Map<String, String> data = new HashMap<>();

        for (String line : lines) {

            Matcher matcher = VDF_PAIR_PATTERN.matcher(line.trim());

            if (matcher.find()) {
                String key = matcher.group(1).toLowerCase();
                String value = matcher.group(2);
                data.put(key, value);
            }

        }

        if (!data.containsKey("appid") || !data.containsKey("name")) {
            log.warn("Skipping invalid manifest {}: Missing appid or name", manifestPath.getFileName());
            return null;
        }

        String appId = data.get("appid");
        String name = data.get("name");

        if (name.equals("Steamworks Common Redistributables")) {
            log.info("Skipping Steamworks Common Redistributables");
            return null;
        }
        String installDirName = data.get("installdir");

        Path installPath = rootLibraryPath.resolve(STEAMAPPS_DIR).resolve(COMMON_DIR).resolve(installDirName);

        boolean isInstalled = Files.exists(installPath);

        LocalDateTime lastPlayed = null;
        if (data.containsKey("lastplayed")) {
            try {
                long unixTime = Long.parseLong(data.get("lastplayed"));
                if (unixTime > 0) {
                    lastPlayed = LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTime), ZoneId.systemDefault());
                }
            } catch (NumberFormatException e) {
                log.debug("Could not parse last played timestamp for {}", name);
            }
        }

        long sizeOnDisk = 0;
        if (data.containsKey("sizeondisk")) {
            try {
                sizeOnDisk = Long.parseLong(data.get("sizeondisk"));
            } catch (NumberFormatException ignored) {
                log.debug("Could not parse size on disk for {}", name);
            }
        }

        Integer playtimeMinutes = 0;

        ScannedLocalGameDTO.ScannedLocalGameDTOBuilder scannedLocalGameDTOBuilder = ScannedLocalGameDTO.builder()
                .title(name)
                .platformGameId(appId)
                .platformName("Steam")
                .platformType(PlatformType.API)
                .installPath(installPath.toString())
                .isInstalled(isInstalled)
                .downloadSize(sizeOnDisk)
                .playtimeMinutes(playtimeMinutes);

        if (lastPlayed != null) scannedLocalGameDTOBuilder.lastPlayed(lastPlayed);

        return scannedLocalGameDTOBuilder.build();
    }

}
