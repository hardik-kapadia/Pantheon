package com.pantheon.backend.service;

import com.pantheon.backend.repository.PlatformRepository;
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
public class LibraryServiceTest {

    private static final Logger log = LoggerFactory.getLogger(LibraryServiceTest.class);

    @Mock
    private PlatformRepository platformRepository;

    @Mock
    private LocalScanNotificationOrchestrationService localScanNotificationOrchestrationService;

    @Mock
    private LibraryScanService libraryScanService;

    private LibraryService libraryService;

    @BeforeEach
    void setUp() {
        libraryService = new LibraryService(platformRepository, libraryScanService, localScanNotificationOrchestrationService);
    }

    @Test
    @DisplayName("Invalid Platform Name: scanPlatform should throw an Illegal Argument exception if invalid name is provided")
    void testInvalidPlatformName() {

        String invalidName = "XYZ";

        when(platformRepository.findByName("XYZ")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> libraryService.scanPlatform(invalidName));

    }

}
