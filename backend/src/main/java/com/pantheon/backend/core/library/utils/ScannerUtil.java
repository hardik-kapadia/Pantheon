package com.pantheon.backend.core.library.utils;

import com.pantheon.backend.core.library.local.LocalGameLibraryScanner;
import com.pantheon.backend.core.platform.model.Platform;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ScannerUtil {

    private final Map<String, LocalGameLibraryScanner> localGameLibraryClientMap;

    public ScannerUtil(List<LocalGameLibraryScanner> localGameLibraryScanners) {
        this.localGameLibraryClientMap = localGameLibraryScanners.stream()
                .collect(Collectors.toMap(LocalGameLibraryScanner::getPlatformName, Function.identity()));
    }

    public LocalGameLibraryScanner getScannerForPlatform(@NonNull Platform platform) throws IllegalStateException {

        LocalGameLibraryScanner scanner = localGameLibraryClientMap.get(platform.getName());

        if (scanner == null) {
            log.error("{}: No LibraryClient found ", platform.getName());
            throw new IllegalStateException("No scanner implementation found for type: " + platform.getName());
        }

        return scanner;
    }
}
