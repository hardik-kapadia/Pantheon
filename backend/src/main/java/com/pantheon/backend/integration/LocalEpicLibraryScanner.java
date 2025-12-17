package com.pantheon.backend.integration;

import com.pantheon.backend.client.LocalGameLibraryClient;
import com.pantheon.backend.dto.ScannedLocalGameDTO;
import com.pantheon.backend.exception.ScanFailureException;
import com.pantheon.backend.model.PlatformType;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
    public List<ScannedLocalGameDTO> scan(Path libraryPath) throws ScanFailureException {
        return List.of();
    }
}
