package com.pantheon.backend.core.library.local;

import com.pantheon.backend.core.inventory.local.dto.ScannedLocalGameDTO;
import com.pantheon.backend.core.library.exception.ScanFailureException;
import com.pantheon.backend.core.platform.model.Platform;
import com.pantheon.backend.core.platform.repository.PlatformRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalGameLibraryScannerTest {

    @Mock
    private PlatformRepository platformRepository;

    private TestLocalGameLibraryScanner scanner;

    @BeforeEach
    void setUp() {
        scanner = new TestLocalGameLibraryScanner(platformRepository);
    }

    @Test
    void getPlatform_FirstCall_FetchesFromRepository() {
        Platform platform = Platform.builder().name("TestPlatform").build();
        when(platformRepository.findByName("TestPlatform")).thenReturn(Optional.of(platform));

        Platform result = scanner.getPlatform();

        assertEquals(platform, result);
        verify(platformRepository).findByName("TestPlatform");
    }

    @Test
    void getPlatform_SubsequentCalls_ReturnCachedPlatform() {
        Platform platform = Platform.builder().name("TestPlatform").build();
        when(platformRepository.findByName("TestPlatform")).thenReturn(Optional.of(platform));

        scanner.getPlatform();
        Platform result = scanner.getPlatform();

        assertEquals(platform, result);
        // Verify repository was called only once
        verify(platformRepository).findByName("TestPlatform");
    }

    @Test
    void getConfiguredLibraryPaths_PlatformExists_ReturnsPaths() {
        Platform platform = Platform.builder()
                .name("TestPlatform")
                .libraryPaths(List.of("/path/1", "/path/2"))
                .build();
        when(platformRepository.findByName("TestPlatform")).thenReturn(Optional.of(platform));

        List<String> paths = scanner.getConfiguredLibraryPaths();

        assertNotNull(paths);
        assertEquals(2, paths.size());
        assertEquals("/path/1", paths.get(0));
    }

    @Test
    void getConfiguredLibraryPaths_PlatformNotFound_ReturnsNull() {
        when(platformRepository.findByName("TestPlatform")).thenReturn(Optional.empty());

        List<String> paths = scanner.getConfiguredLibraryPaths();

        assertNull(paths);
    }

    @Test
    void refreshPlatform_ClearsCache() {
        Platform platform = Platform.builder().name("TestPlatform").build();
        when(platformRepository.findByName("TestPlatform")).thenReturn(Optional.of(platform));

        scanner.getPlatform(); // Cache it
        scanner.refreshPlatform(); // Clear it
        scanner.getPlatform(); // Fetch again

        verify(platformRepository, org.mockito.Mockito.times(2)).findByName("TestPlatform");
    }

    // Concrete implementation for testing abstract class
    static class TestLocalGameLibraryScanner extends LocalGameLibraryScanner {

        protected TestLocalGameLibraryScanner(PlatformRepository platformRepository) {
            super(platformRepository);
        }

        @Override
        public String getPlatformName() {
            return "TestPlatform";
        }

        @Override
        public List<ScannedLocalGameDTO> scan(Path path) throws ScanFailureException {
            return Collections.emptyList();
        }
    }
}
