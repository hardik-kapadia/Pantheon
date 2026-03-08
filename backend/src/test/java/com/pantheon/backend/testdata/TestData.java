package com.pantheon.backend.testdata;

import com.pantheon.backend.core.library.model.Library;
import com.pantheon.backend.core.platform.model.Platform;

public interface TestData {

    String LIBRARY_PATH_STEAM_ACTUAL = "E://Play//Steam//common//";

    Platform PLATFORM = Platform.builder()
            .name("Test Platform")
            .build();

    Platform PLATFORM_STEAM = Platform.builder()
            .name("Steam")
            .build();

    Platform PLATFORM_STEAM_WITH_LIBRARY = createSteamPlatform();

    Platform PLATFORM_EPIC = Platform.builder()
            .name("Epic Games")
            .build();

    Library LIBRARY_STEAM_1 = PLATFORM_STEAM_WITH_LIBRARY.getLibraries().getFirst();

    static Platform createSteamPlatform() {
        Platform platform = Platform.builder()
                .name("Steam")
                .build();

        Library library = Library.builder()
                .platform(platform)
                .path(LIBRARY_PATH_STEAM_ACTUAL)
                .build();
        platform.getLibraries().add(library);
        return platform;
    }
}
