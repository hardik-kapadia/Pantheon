package com.pantheon.backend.worker;

import com.pantheon.backend.core.localscanner.LocalGameLibraryScanner;
import com.pantheon.backend.dto.ScannedLocalGameDTO;
import com.pantheon.backend.exception.ScanFailureException;
import com.pantheon.backend.model.PlatformType;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
class LocalGogLibraryScanner implements LocalGameLibraryScanner {
    @Override
    public PlatformType getSupportedType() {
        return PlatformType.API;
    }

    @Override
    public String getPlatformName() {
        return "GOG";
    }

    @Override
    public List<ScannedLocalGameDTO> scan(Path libraryPath) throws ScanFailureException {
        return List.of();
    }
}
