package com.pantheon.backend.core.platform.api;

import com.pantheon.backend.core.platform.local.dto.PlatformDTO;
import com.pantheon.backend.core.platform.dto.PlatformSetupDTO;
import com.pantheon.backend.core.platform.local.PlatformLocalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class PlatformController {

    /**
     * Mappings in here: Adding a new platform (Generic Type)
     * Adding a new Platform: Steam/ Epic/ Gog
     */

    private final PlatformLocalService platformLocalService;

    /**
     *
     * Used to create generic local libraries not associated with steam/ epic/ gog, etc.
     *
     * @return ResponseEntity<String>
     */
    @PostMapping("/create")
    public ResponseEntity<String> createPlatform() {

        // TODO: Add functionality for generic platforms (starting from the scanner implementations)

        return null;
    }

    /**
     *
     * Used to configure local libraries associated with steam/ epic/ gog, etc.
     *
     * @return ResponseEntity<PlatformDTO>
     */
    @PostMapping("/setup")
    public ResponseEntity<PlatformDTO> configurePlatform(PlatformSetupDTO platformSetupDTO) throws IllegalArgumentException {

        if (platformSetupDTO.name().isBlank() || platformSetupDTO.executablePath().isBlank()) {

            String missing = platformSetupDTO.executablePath().isBlank() ? "Executable Path" : "Name";

            throw new IllegalArgumentException(missing + " is missing");
        }

        return ResponseEntity.ok(platformLocalService.setupLocalPlatform(platformSetupDTO));

    }

}
