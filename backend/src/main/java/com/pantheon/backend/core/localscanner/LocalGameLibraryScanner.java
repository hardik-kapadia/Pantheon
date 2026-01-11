package com.pantheon.backend.core.localscanner;

import com.pantheon.backend.dto.ScannedLocalGameDTO;
import com.pantheon.backend.exception.ScanFailureException;
import com.pantheon.backend.model.PlatformType;

import java.nio.file.Path;
import java.util.List;

public interface LocalGameLibraryScanner {

    PlatformType getSupportedType();

    String getPlatformName();

    List<ScannedLocalGameDTO> scan(Path libraryPath) throws ScanFailureException;

}
