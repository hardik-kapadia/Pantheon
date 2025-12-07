package com.pantheon.backend.client;

import com.pantheon.backend.dto.ScannedGameDTO;
import com.pantheon.backend.model.PlatformType;

import java.nio.file.Path;
import java.util.List;

public interface LocalGameLibraryClient {

    PlatformType getSupportedType();

    String getPlatformName();

    List<ScannedGameDTO> scan(Path libraryPath);

}
