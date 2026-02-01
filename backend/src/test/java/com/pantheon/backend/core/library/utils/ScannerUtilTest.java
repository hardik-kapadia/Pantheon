package com.pantheon.backend.core.library.utils;

import com.pantheon.backend.core.library.local.LocalGameLibraryScanner;
import com.pantheon.backend.core.platform.model.Platform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScannerUtilTest {

    @Mock
    private LocalGameLibraryScanner steamScanner;

    @Mock
    private LocalGameLibraryScanner epicScanner;

    private ScannerUtil scannerUtil;

    @BeforeEach
    void setUp() {
        // We need to stub getPlatformName() before creating ScannerUtil because it's used in the constructor
        // However, we can't stub strict mocks before they are used if we want to test the "missing" case cleanly without unnecessary stubs.
        // So we'll create the ScannerUtil inside the tests or use lenient stubbing if needed.
    }

    @Test
    void getScannerForPlatform_ReturnsCorrectScanner() {
        when(steamScanner.getPlatformName()).thenReturn("Steam");
        when(epicScanner.getPlatformName()).thenReturn("Epic");

        scannerUtil = new ScannerUtil(List.of(steamScanner, epicScanner));

        Platform steamPlatform = Platform.builder().name("Steam").build();
        Platform epicPlatform = Platform.builder().name("Epic").build();

        assertEquals(steamScanner, scannerUtil.getScannerForPlatform(steamPlatform));
        assertEquals(epicScanner, scannerUtil.getScannerForPlatform(epicPlatform));
    }

    @Test
    void getScannerForPlatform_UnknownPlatform_ThrowsException() {
        when(steamScanner.getPlatformName()).thenReturn("Steam");
        
        scannerUtil = new ScannerUtil(List.of(steamScanner));

        Platform unknownPlatform = Platform.builder().name("Unknown").build();

        assertThrows(IllegalStateException.class, () -> scannerUtil.getScannerForPlatform(unknownPlatform));
    }

    @Test
    void getScannerForPlatform_EmptyScannersList_ThrowsException() {
        scannerUtil = new ScannerUtil(Collections.emptyList());

        Platform steamPlatform = Platform.builder().name("Steam").build();

        assertThrows(IllegalStateException.class, () -> scannerUtil.getScannerForPlatform(steamPlatform));
    }
    
    @Test
    void getScannerForPlatform_NullPlatform_ThrowsNullPointerException() {
        scannerUtil = new ScannerUtil(Collections.emptyList());
        assertThrows(NullPointerException.class, () -> scannerUtil.getScannerForPlatform(null));
    }
}
