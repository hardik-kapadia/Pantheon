package com.pantheon.backend.external.scanner.local;

import com.pantheon.backend.core.library.local.LocalGameLibraryScanner;
import com.pantheon.backend.core.inventory.local.dto.ScannedLocalGameDTO;
import com.pantheon.backend.core.library.exception.ScanFailureException;
import com.pantheon.backend.core.platform.repository.PlatformRepository;
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
    public List<ScannedLocalGameDTO> scan(Path path) throws ScanFailureException {
        return List.of();
    }
}
