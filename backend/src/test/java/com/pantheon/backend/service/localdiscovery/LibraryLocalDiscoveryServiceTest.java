package com.pantheon.backend.service.localdiscovery;

import com.pantheon.backend.repository.PlatformRepository;
import com.pantheon.backend.service.librarydiscovery.helper.PlatformLocalScanService;
import com.pantheon.backend.service.librarydiscovery.notification.LocalScanNotificationOrchestrationService;
import com.pantheon.backend.service.librarydiscovery.LibraryLocalDiscoveryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LibraryLocalDiscoveryServiceTest {

    private static final Logger log = LoggerFactory.getLogger(LibraryLocalDiscoveryServiceTest.class);

    @Mock
    private PlatformRepository platformRepository;

    @Mock
    private LocalScanNotificationOrchestrationService localScanNotificationOrchestrationService;

    @Mock
    private PlatformLocalScanService platformLocalScanService;

    private LibraryLocalDiscoveryService libraryLocalDiscoveryService;

    @BeforeEach
    void setUp() {
        libraryLocalDiscoveryService = new LibraryLocalDiscoveryService(platformRepository, platformLocalScanService, localScanNotificationOrchestrationService);
    }

    @Test
    @DisplayName("Invalid Platform Name: scanPlatform should throw an Illegal Argument exception if invalid name is provided")
    void testInvalidPlatformName() {

        String invalidName = "XYZ";

        when(platformRepository.findByName("XYZ")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> libraryLocalDiscoveryService.scanPlatform(invalidName));

    }

}
