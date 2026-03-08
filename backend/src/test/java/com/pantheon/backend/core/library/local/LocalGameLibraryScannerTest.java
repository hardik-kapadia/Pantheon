package com.pantheon.backend.core.library.local;

import com.pantheon.backend.core.inventory.local.dto.ScannedLocalGameDTO;
import com.pantheon.backend.core.library.exception.ScanFailureException;
import com.pantheon.backend.core.platform.PlatformService;
import com.pantheon.backend.core.platform.io.GetPlatformByNameRequest;
import com.pantheon.backend.core.platform.io.GetPlatformByNameResponse;
import com.pantheon.backend.core.platform.model.Platform;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;


import static com.pantheon.backend.testdata.TestData.LIBRARY_PATH_STEAM_ACTUAL;
import static com.pantheon.backend.testdata.TestData.PLATFORM_STEAM_WITH_LIBRARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalGameLibraryScannerTest {

    @Mock
    private PlatformService platformService;

    private TestLocalGameLibraryScanner scanner;

    private static final GetPlatformByNameRequest PLATFORM_REQUEST_STEAM = GetPlatformByNameRequest.builder()
            .name("Steam")
            .build();

    private static final GetPlatformByNameResponse PLATFORM_RESPONSE_SUCCESS_STEAM = GetPlatformByNameResponse.Success.builder()
            .platform(PLATFORM_STEAM_WITH_LIBRARY)
            .build();

    private static final GetPlatformByNameResponse PLATFORM_RESPONSE_FAILURE = GetPlatformByNameResponse.InvalidInput.builder()
            .message("Invalid platform name")
            .build();

    @BeforeEach
    void setUp() {
        scanner = new TestLocalGameLibraryScanner(platformService);
    }

    @AfterEach
    void cleanUp() {
        verifyNoInteractions(platformService);
    }


    @Nested
    class GetPlatformByName {

        @Test
        void getPlatform_FirstCall_FetchesFromRepository() {
            when(platformService.getPlatform(argThat(req -> req.name().equals("Steam"))))
                    .thenReturn(PLATFORM_RESPONSE_SUCCESS_STEAM);

            Platform result = scanner.getPlatform();

            assertEquals(PLATFORM_STEAM_WITH_LIBRARY, result);
            verify(platformService).getPlatform(PLATFORM_REQUEST_STEAM);
        }

        @Test
        void getPlatform_SubsequentCalls_ReturnCachedPlatform() {
            when(platformService.getPlatform(argThat(req -> req.name().equals("Steam"))))
                    .thenReturn(PLATFORM_RESPONSE_SUCCESS_STEAM);

            scanner.getPlatform();
            Platform result = scanner.getPlatform();

            assertEquals(PLATFORM_STEAM_WITH_LIBRARY, result);
            verify(platformService).getPlatform(PLATFORM_REQUEST_STEAM);
        }
    }

    @Nested
    class GetConfiguredLibraryPaths {
        @Test
        void getConfiguredLibraryPaths_PlatformExists_ReturnsPaths() {

            when(platformService.getPlatform(argThat(req -> req.name().equals("Steam"))))
                    .thenReturn(PLATFORM_RESPONSE_SUCCESS_STEAM);

            List<String> paths = scanner.getConfiguredLibraryPaths();

            assertThat(paths)
                    .isNotNull()
                    .hasSize(1)
                    .containsExactly(LIBRARY_PATH_STEAM_ACTUAL);

        }

        @Test
        void getConfiguredLibraryPaths_PlatformNotFound_throwsIllegalArgumentException() {

            when(platformService.getPlatform(argThat(req -> req.name().equals("Steam"))))
                    .thenReturn(PLATFORM_RESPONSE_FAILURE);

            assertThrows(IllegalArgumentException.class, () -> scanner.getConfiguredLibraryPaths());
        }
    }

    @Nested
    class RefreshPlatform {
        @Test
        void refreshPlatform_ClearsCache() {
            when(platformService.getPlatform(argThat(req -> req.name().equals("Steam"))))
                    .thenReturn(PLATFORM_RESPONSE_SUCCESS_STEAM);

            scanner.getPlatform(); // Cache it
            scanner.refreshPlatform(); // Clear it
            scanner.getPlatform(); // Fetch again

            verify(platformService, org.mockito.Mockito.times(2))
                    .getPlatform(argThat(req -> req.name().equals("Steam")));
        }
    }

    // Concrete implementation for testing abstract class
    static class TestLocalGameLibraryScanner extends LocalGameLibraryScanner {

        protected TestLocalGameLibraryScanner(PlatformService platformService) {
            super(platformService);
        }

        @Override
        public String getPlatformName() {
            return "Steam";
        }

        @Override
        public List<ScannedLocalGameDTO> scan(Path path) {
            return Collections.emptyList();
        }
    }
}
