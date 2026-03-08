package com.pantheon.backend.core.library.local;

import com.pantheon.backend.core.inventory.local.dto.ScannedLocalGameDTO;
import com.pantheon.backend.core.library.exception.ScanFailureException;
import com.pantheon.backend.core.library.model.Library;
import com.pantheon.backend.core.platform.PlatformService;
import com.pantheon.backend.core.platform.io.GetPlatformByNameRequest;
import com.pantheon.backend.core.platform.io.GetPlatformByNameResponse;
import com.pantheon.backend.core.platform.model.Platform;

import java.nio.file.Path;
import java.util.List;

public abstract class LocalGameLibraryScanner {

    private final PlatformService platformService;

    private Platform cachedPlatform;

    protected LocalGameLibraryScanner(PlatformService platformService) {
        this.platformService = platformService;
    }

    public abstract String getPlatformName();

    public abstract List<ScannedLocalGameDTO> scan(Path path) throws ScanFailureException;

    protected Platform getPlatform() {
        if (this.cachedPlatform == null) {

            String name = getPlatformName();

            var getPlatformByNameRequest = GetPlatformByNameRequest.builder().name(name).build();
            var getPlatformByNameResponse = platformService.getPlatform(getPlatformByNameRequest);

            switch (getPlatformByNameResponse) {
                case GetPlatformByNameResponse.Success success -> this.cachedPlatform = success.platform();
                case GetPlatformByNameResponse.InvalidInput invalidInput -> throw new IllegalArgumentException(invalidInput.message());
            }

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
