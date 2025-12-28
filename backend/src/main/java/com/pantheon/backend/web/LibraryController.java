package com.pantheon.backend.web;

import com.pantheon.backend.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Arrays;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;
    private final SsePubSub ssePubSub;

    @PostMapping("/scan/{platform}")
    public ResponseEntity<String> scanPlatform(@PathVariable String platform) {
        libraryService.scanPlatform(platform);
        return ResponseEntity.accepted().body("Scan initiated for " + platform);
    }

    @PostMapping("/scan/{platforms}")
    public ResponseEntity<String> scanPlatforms(@PathVariable String[] platforms) {
//        libraryService.scanPlatform(platform);
        return ResponseEntity.accepted().body("Scan initiated for " + Arrays.toString(platforms));
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents() {
        return ssePubSub.subscribe();
    }
}
