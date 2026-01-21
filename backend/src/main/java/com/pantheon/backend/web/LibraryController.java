package com.pantheon.backend.web;

import com.pantheon.backend.service.librarydiscovery.local.LibraryLocalDiscoveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Arrays;

@RestController
@RequestMapping("/api/library/discovery")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryLocalDiscoveryService libraryLocalDiscoveryService;
    private final SsePubSub ssePubSub;

    @PostMapping("/scan/{platform}")
    public ResponseEntity<String> scanPlatform(@PathVariable String platform) {
        libraryLocalDiscoveryService.scanPlatform(platform);
        return ResponseEntity.accepted().body("Scan initiated for " + platform);
    }

    @PostMapping("/scan")
    public ResponseEntity<String> scanPlatforms(@RequestParam(required = false) String[] platforms) {

        libraryLocalDiscoveryService.scanPlatforms(platforms);

        String responseMessage = "Scan Initiated for all platforms";

        if (platforms != null && platforms.length > 0)
            responseMessage = "Scan Initiated for " + Arrays.toString(platforms);

        return ResponseEntity.accepted().body(responseMessage);
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents() {
        return ssePubSub.subscribe();
    }
}
