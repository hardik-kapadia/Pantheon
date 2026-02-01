package com.pantheon.backend.core.library.local;

import com.pantheon.backend.core.inventory.local.dto.ScannedLocalGameDTO;
import com.pantheon.backend.core.library.exception.ScanFailureException;
import com.pantheon.backend.core.platform.model.Platform;
import com.pantheon.backend.core.platform.repository.PlatformRepository;

import java.nio.file.Path;
import java.util.List;

public abstract class LocalGameLibraryScanner {

    private final PlatformRepository platformRepository;

    private Platform cachedPlatform;

    protected LocalGameLibraryScanner(PlatformRepository platformRepository) {
        this.platformRepository = platformRepository;
    }

    public abstract String getPlatformName();

    public abstract List<ScannedLocalGameDTO> scan(Path libraryPath) throws ScanFailureException;

    protected Platform getPlatform() {
        if (this.cachedPlatform == null) {

            String name = getPlatformName();

            this.cachedPlatform = platformRepository.findByName(name).orElse(null);

        }
        return this.cachedPlatform;
    }

    public List<String> getConfiguredLibraryPaths() {

        Platform platform = getPlatform();

        if (platform == null) {
            return null;
        }

        return platform.getLibraryPaths();

    }

    public void refreshPlatform() {
        this.cachedPlatform = null;
    }


}
