package com.pantheon.backend.integration;

import com.pantheon.backend.client.LocalGameLibraryClient;
import com.pantheon.backend.dto.ScannedGameDTO;
import com.pantheon.backend.model.PlatformType;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
class LocalEpicLibraryScanner implements LocalGameLibraryClient {
    @Override
    public PlatformType getSupportedType() {
        return PlatformType.API;
    }

    @Override
    public String getPlatformName() {
        return "Epic";
    }

    @Override
    public List<ScannedGameDTO> scan(Path libraryPath) {
        return List.of();
    }
}
