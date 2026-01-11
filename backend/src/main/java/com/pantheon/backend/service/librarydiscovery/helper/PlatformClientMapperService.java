package com.pantheon.backend.service.librarydiscovery.helper;

import com.pantheon.backend.core.localscanner.LocalGameLibraryScanner;
import com.pantheon.backend.model.Platform;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PlatformClientMapperService {


    private final Map<String, LocalGameLibraryScanner> localGameLibraryClientMap;

    @Autowired
    public PlatformClientMapperService(List<LocalGameLibraryScanner> localGameLibraryScanners) {
        this.localGameLibraryClientMap = localGameLibraryScanners.stream()
                .collect(Collectors.toMap(LocalGameLibraryScanner::getPlatformName, Function.identity()));
    }

    public LocalGameLibraryScanner getScanner(@NonNull Platform platform) throws IllegalStateException {

        LocalGameLibraryScanner scanner = localGameLibraryClientMap.get(platform.getName());

        if (scanner == null) {
            log.error("{}: No LibraryClient found ", platform.getName());
            throw new IllegalStateException("No scanner implementation found for type: " + platform.getName());
        }

        return scanner;
    }

}
