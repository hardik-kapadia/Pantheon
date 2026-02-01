package com.pantheon.backend.core.platform.local;

import com.pantheon.backend.core.library.local.LocalGameLibraryScanner;
import com.pantheon.backend.core.library.utils.ScannerUtil;
import com.pantheon.backend.core.platform.dto.PlatformDTO;
import com.pantheon.backend.core.platform.dto.PlatformSetupDTO;
import com.pantheon.backend.core.platform.model.Platform;
import com.pantheon.backend.core.platform.model.PlatformType;
import com.pantheon.backend.core.platform.repository.PlatformRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformLocalServiceTest {

    @Mock
    private PlatformRepository platformRepository;

    @Mock
    private ScannerUtil scannerUtil;

    @InjectMocks
    private PlatformLocalService platformLocalService;

    private Platform steamPlatform;

    @BeforeEach
    void setUp() {
        steamPlatform = Platform.builder()
                .name("Steam")
                .type(PlatformType.API)
                .build();
    }

    @Test
    void getAllPlatforms_ReturnsAllPlatforms() {
        when(platformRepository.findAll()).thenReturn(Arrays.asList(steamPlatform));

        List<Platform> result = platformLocalService.getAllPlatforms();

        assertEquals(1, result.size());
        assertEquals("Steam", result.get(0).getName());
    }

    @Test
    void getPlatformByName_ExistingPlatform_ReturnsPlatform() {
        when(platformRepository.findByName("Steam")).thenReturn(Optional.of(steamPlatform));

        Platform result = platformLocalService.getPlatformByName("Steam");

        assertNotNull(result);
        assertEquals("Steam", result.getName());
    }

    @Test
    void getPlatformByName_NonExistingPlatform_ThrowsException() {
        when(platformRepository.findByName("Unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> platformLocalService.getPlatformByName("Unknown"));
    }

    @Test
    void setupLocalPlatform_ValidDTO_UpdatesAndReturnsPlatform() {
        PlatformSetupDTO setupDTO = new PlatformSetupDTO("Steam", "/path/to/exe", List.of("/lib/path"), "icon.png");
        
        when(platformRepository.findByName("Steam")).thenReturn(Optional.of(steamPlatform));
        when(platformRepository.save(any(Platform.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        LocalGameLibraryScanner mockScanner = mock(LocalGameLibraryScanner.class);
        when(scannerUtil.getScannerForPlatform(any(Platform.class))).thenReturn(mockScanner);

        PlatformDTO result = platformLocalService.setupLocalPlatform(setupDTO);

        assertNotNull(result);
        assertEquals("Steam", result.name());
        assertEquals("/path/to/exe", result.executablePath());
        assertEquals(1, result.libraryPaths().size());
        assertEquals("/lib/path", result.libraryPaths().get(0));
        assertEquals("icon.png", result.iconUrl());
        assertEquals(PlatformType.API, result.platformType());

        verify(mockScanner).refreshPlatform();
    }
    
    @Test
    void setupLocalPlatform_PlatformNotFound_ThrowsException() {
        PlatformSetupDTO setupDTO = new PlatformSetupDTO("Unknown", "/path/to/exe", List.of("/lib/path"), "icon.png");
        when(platformRepository.findByName("Unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> platformLocalService.setupLocalPlatform(setupDTO));
    }
}
