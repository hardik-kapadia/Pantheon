package com.pantheon.backend.core.library.local;

import com.pantheon.backend.core.inventory.local.dto.ScannedLocalGameDTO;
import com.pantheon.backend.core.library.exception.ScanFailureException;
import com.pantheon.backend.core.library.model.Library;
import com.pantheon.backend.core.platform.PlatformService;
import com.pantheon.backend.core.platform.model.Platform;
import com.pantheon.backend.core.platform.PlatformRepository;

import java.nio.file.Path;
import java.util.List;

public abstract class LocalGameLibraryScanner {

    private final PlatformService platformService;

    private Platform cachedPlatform;

    protected LocalGameLibraryScanner(PlatformService platformService, PlatformService platformService1) {
        this.platformService = platformService1;
    }

    public abstract String getPlatformName();

    public abstract List<ScannedLocalGameDTO> scan(Path path) throws ScanFailureException;

    protected Platform getPlatform() {
        if (this.cachedPlatform == null) {

            String name = getPlatformName();

            this.cachedPlatform = platformService.getPlatformByName(name).orElse(null);

        }
        return this.cachedPlatform;
    }

    public List<String> getConfiguredLibraryPaths() {

        Platform platform = getPlatform();

        if (platform == null) {
            return null;
        }

        return platform.getLibraries().stream().map(Library::getPath).toList();

    }

    public void refreshPlatform() {
        this.cachedPlatform = null;
    }


}
