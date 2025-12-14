package com.pantheon.backend.client;

import com.pantheon.backend.dto.ScannedLocalGameDTO;
import com.pantheon.backend.model.PlatformType;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface LocalGameLibraryClient {

    PlatformType getSupportedType();

    String getPlatformName();

    List<ScannedLocalGameDTO> scan(Path libraryPath) throws IOException;

}
