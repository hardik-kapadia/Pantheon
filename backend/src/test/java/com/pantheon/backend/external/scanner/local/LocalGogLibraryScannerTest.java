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

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class LocalGogLibraryScannerTest {

    @Mock
    private PlatformRepository platformRepository;

    private LocalGogLibraryScanner scanner;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        scanner = new LocalGogLibraryScanner(platformRepository);
    }

    @Test
    void getPlatformName_ReturnsGOG() {
        assertEquals("GOG", scanner.getPlatformName());
    }

    @Test
    void scan_ReturnsEmptyList() throws ScanFailureException {
        List<ScannedLocalGameDTO> result = scanner.scan(tempDir);
        assertTrue(result.isEmpty());
    }
}
