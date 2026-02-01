package com.pantheon.backend.external.provider.local;

import com.pantheon.backend.core.library.local.LocalGameLibraryScanner;
import com.pantheon.backend.core.inventory.local.dto.ScannedLocalGameDTO;
import com.pantheon.backend.core.library.exception.ScanFailureException;
import com.pantheon.backend.repository.PlatformRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
class LocalEpicLibraryScanner extends LocalGameLibraryScanner {

    @Autowired
    protected LocalEpicLibraryScanner(PlatformRepository platformRepository) {
        super(platformRepository);
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
