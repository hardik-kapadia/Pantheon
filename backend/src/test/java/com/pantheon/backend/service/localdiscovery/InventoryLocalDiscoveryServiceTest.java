package com.pantheon.backend.service.localdiscovery;

import com.pantheon.backend.core.platform.repository.PlatformRepository;
import com.pantheon.backend.core.inventory.local.InventoryLocalDiscoveryService;
import com.pantheon.backend.core.notification.LocalScanNotificationOrchestrationService;
import com.pantheon.backend.core.inventory.local.processor.InventoryLocalScanService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InventoryLocalDiscoveryServiceTest {

    private static final Logger log = LoggerFactory.getLogger(InventoryLocalDiscoveryServiceTest.class);

    @Mock
    private PlatformRepository platformRepository;

    @Mock
    private LocalScanNotificationOrchestrationService localScanNotificationOrchestrationService;

    @Mock
    private InventoryLocalScanService inventoryLocalScanService;

    @InjectMocks
    private InventoryLocalDiscoveryService inventoryLocalDiscoveryService;

    @Test
    @DisplayName("Invalid Platform Name: scanPlatform should throw an Illegal Argument exception if invalid name is provided")
    void testInvalidPlatformName() {

        String invalidName = "XYZ";

        when(platformRepository.findByName("XYZ")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> inventoryLocalDiscoveryService.scanPlatform(invalidName));

    }

}
