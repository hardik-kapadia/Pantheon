package com.pantheon.backend.core.platform.api;

import com.pantheon.backend.core.platform.PlatformService;
import com.pantheon.backend.core.platform.dto.PlatformSetupDTO;
import com.pantheon.backend.core.platform.model.PlatformType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformControllerTest {

    @Mock
    private PlatformService platformService;

    @InjectMocks
    private PlatformController platformController;

    @Test
    void createPlatform_ReturnsNotImplemented() {
        ResponseEntity<String> response = platformController.createPlatform();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Not implemented yet", response.getBody());
    }

}
