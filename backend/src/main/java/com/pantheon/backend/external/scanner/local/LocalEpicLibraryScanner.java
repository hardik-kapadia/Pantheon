package com.pantheon.backend.external.scanner.local;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pantheon.backend.core.inventory.local.dto.ScannedLocalGameDTO;
import com.pantheon.backend.core.library.exception.ConfigurationException;
import com.pantheon.backend.core.library.exception.ScanFailureException;
import com.pantheon.backend.core.library.local.LocalGameLibraryScanner;
import com.pantheon.backend.core.platform.model.Platform;
import com.pantheon.backend.core.platform.repository.PlatformRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


@Slf4j
@Component
class LocalEpicLibraryScanner extends LocalGameLibraryScanner {

    private final String MANIFEST_EXT = ".item";

    private final Platform platform;
    private final ObjectMapper objectMapper;

    @Autowired
    protected LocalEpicLibraryScanner(PlatformRepository platformRepository) {
        super(platformRepository);
        this.objectMapper = new ObjectMapper();
        this.platform = super.getPlatform();
    }

    @Override
    public String getPlatformName() {
        return "Epic";
    }

    @Override
    public List<ScannedLocalGameDTO> scan(Path manifestsPath) throws ScanFailureException, ConfigurationException {

//        String epicManifestsPathStr = this.platform.getManifestsPath();
//
//        if (epicManifestsPathStr == null)
//            throw new ConfigurationException("EpicManifestsPath is not configured");
//
//        log.info("Epic Scanner: Scanning Manifests directory {}", epicManifestsPathStr);
//
//        Path manifestPath = Path.of(epicManifestsPathStr);
        if (!Files.exists(manifestsPath) || !Files.isDirectory(manifestsPath)) {
            log.warn("Epic Manifests path not found at {}", manifestsPath);
            throw new ConfigurationException("Epic Manifests path not found at" + manifestsPath);
        }

        List<Path> manifests;

        try (Stream<Path> stream = Files.list(manifestsPath)) {
            manifests = stream.filter(path -> !Files.isDirectory(path) && Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && path.getFileName().toString().endsWith(MANIFEST_EXT)).toList();
        } catch (IOException e) {
            throw new ScanFailureException("Failed to list files in Epic Manifest Path: " + e.getMessage(), e);
        }

        log.info("Epic Scanner: Found {} manifest files in {}", manifests.size(), manifestsPath);

        List<ScannedLocalGameDTO> foundGames = new ArrayList<>();

        for (Path manifestPath : manifests) {

            try {
                ScannedLocalGameDTO scannedLocalGameDTO = parseManifest(manifestPath);
                if (scannedLocalGameDTO != null) foundGames.add(scannedLocalGameDTO);
            } catch (IOException e) {
                log.error("Failed to parse Epic manifest: {}. Error: {}", manifestPath.getFileName(), e.getMessage());
            }

        }

        return foundGames;
    }

    private ScannedLocalGameDTO parseManifest(Path manifestPath) throws IOException {

        log.info("Parsing Epic manifest file {}", manifestPath);

        try {
            JsonNode rootNode = objectMapper.readTree(manifestPath.toFile());

            String title = rootNode.path("DisplayName").asText(null);
            String appName = rootNode.path("AppName").asText(null);
            String installLocation = rootNode.path("InstallLocation").asText(null);
            long installSize = rootNode.path("InstallSize").asLong(0L);

            if (title == null || appName == null || installLocation == null) {
                log.warn("Epic manifest {} is missing required fields, skipping.", manifestPath.getFileName());
                return null;
            }

            Path gameFolder = Path.of(installLocation);
            boolean isInstalled = Files.exists(gameFolder) && Files.isDirectory(gameFolder);

            return ScannedLocalGameDTO.builder()
                    .title(title)
                    .platformGameId(appName)
                    .platformName(getPlatformName())
                    .platformType(platform.getType())
                    .installPath(installLocation)
                    .isInstalled(isInstalled)
                    .downloadSize(installSize)
                    .playtimeMinutes(0)
                    .lastPlayed(null)
                    .build();

        } catch (IOException e) {
            log.error("Failed to parse Epic manifest JSON: {}. Error: {}", manifestPath.getFileName(), e.getMessage());
            return null;
        }
    }
}
