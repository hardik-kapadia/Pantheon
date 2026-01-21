package com.pantheon.backend.provider.local;

import com.pantheon.backend.core.localscanner.LocalGameLibraryScanner;
import com.pantheon.backend.dto.ScannedLocalGameDTO;
import com.pantheon.backend.exception.ScanFailureException;
import com.pantheon.backend.repository.PlatformRepository;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
class LocalGogLibraryScanner extends LocalGameLibraryScanner {

    protected LocalGogLibraryScanner(PlatformRepository platformRepository) {
        super(platformRepository);
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
