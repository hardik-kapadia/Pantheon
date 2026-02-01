package com.pantheon.backend.external.scanner.local;

import com.pantheon.backend.core.inventory.local.dto.ScannedLocalGameDTO;
import com.pantheon.backend.core.library.exception.ScanFailureException;
import com.pantheon.backend.core.platform.repository.PlatformRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class LocalSteamLibraryScannerTest {

    @Mock
    private PlatformRepository platformRepository;

    private LocalSteamLibraryScanner scanner;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        scanner = new LocalSteamLibraryScanner(platformRepository);
    }

    @Test
    void getPlatformName_ReturnsSteam() {
        assertEquals("Steam", scanner.getPlatformName());
    }

    @Test
    void scan_EmptyDirectory_ReturnsEmptyList() throws ScanFailureException {
        List<ScannedLocalGameDTO> result = scanner.scan(tempDir);
        assertTrue(result.isEmpty());
    }

    @Test
    void scan_ValidLibrary_ReturnsGames() throws IOException, ScanFailureException {
        // Setup directory structure
        Path steamApps = tempDir.resolve("steamapps");
        Path common = steamApps.resolve("common");
        Files.createDirectories(common);

        // Create manifest file
        Path manifest = steamApps.resolve("appmanifest_12345.acf");
        String manifestContent = """
                "AppState"
                {
                    "appid"     "12345"
                    "name"      "Test Game"
                    "installdir" "TestGameDir"
                    "lastplayed" "1672531200"
                    "sizeondisk" "1024000"
                }
                """;
        Files.writeString(manifest, manifestContent);

        // Create game directory
        Files.createDirectories(common.resolve("TestGameDir"));

        List<ScannedLocalGameDTO> result = scanner.scan(tempDir);

        assertEquals(1, result.size());
        ScannedLocalGameDTO game = result.get(0);
        assertEquals("Test Game", game.title());
        assertEquals("12345", game.platformGameId());
        assertEquals("Steam", game.platformName());
        assertTrue(game.isInstalled());
        assertEquals(1024000L, game.downloadSize());
    }

    @Test
    void scan_MissingInstallDir_ReturnsGameAsNotInstalled() throws IOException, ScanFailureException {
        // Setup directory structure
        Path steamApps = tempDir.resolve("steamapps");
        Files.createDirectories(steamApps);

        // Create manifest file
        Path manifest = steamApps.resolve("appmanifest_12345.acf");
        String manifestContent = """
                "AppState"
                {
                    "appid"     "12345"
                    "name"      "Test Game"
                    "installdir" "TestGameDir"
                }
                """;
        Files.writeString(manifest, manifestContent);

        List<ScannedLocalGameDTO> result = scanner.scan(tempDir);

        assertEquals(1, result.size());
        assertFalse(result.get(0).isInstalled());
    }

    @Test
    void scan_InvalidManifest_SkipsGame() throws IOException, ScanFailureException {
        Path steamApps = tempDir.resolve("steamapps");
        Files.createDirectories(steamApps);

        Path manifest = steamApps.resolve("appmanifest_12345.acf");
        Files.writeString(manifest, "Invalid Content");

        List<ScannedLocalGameDTO> result = scanner.scan(tempDir);

        assertTrue(result.isEmpty());
    }
    
    @Test
    void scan_SteamworksCommonRedistributables_SkipsGame() throws IOException, ScanFailureException {
        Path steamApps = tempDir.resolve("steamapps");
        Files.createDirectories(steamApps);

        Path manifest = steamApps.resolve("appmanifest_228980.acf");
        String manifestContent = """
                "AppState"
                {
                    "appid"     "228980"
                    "name"      "Steamworks Common Redistributables"
                    "installdir" "Steamworks Shared"
                }
                """;
        Files.writeString(manifest, manifestContent);

        List<ScannedLocalGameDTO> result = scanner.scan(tempDir);

        assertTrue(result.isEmpty());
    }
}
