package com.pantheon.backend.core.platform.api;

import com.pantheon.backend.core.platform.PlatformService;
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
    private final PlatformService platformService;

    /**
     *
     * Used to create generic local libraries not associated with steam/ epic/ gog, etc.
     *
     * @return ResponseEntity<String>
     */
    @PostMapping("/create")
    public ResponseEntity<String> createPlatform() {
        return ResponseEntity.internalServerError().body("Not implemented yet");
    }

}
