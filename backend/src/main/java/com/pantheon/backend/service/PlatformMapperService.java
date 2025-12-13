package com.pantheon.backend.service;

import com.pantheon.backend.client.LocalGameLibraryClient;
import com.pantheon.backend.model.Platform;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PlatformMapperService {


    private final Map<String, LocalGameLibraryClient> scannerMap;

    @Autowired
    public PlatformMapperService(List<LocalGameLibraryClient> scanners) {
        this.scannerMap = scanners.stream()
                .collect(Collectors.toMap(LocalGameLibraryClient::getPlatformName, Function.identity()));
    }

    public LocalGameLibraryClient getScanner(@NonNull Platform platform) throws IllegalStateException {

        LocalGameLibraryClient scanner = scannerMap.get(platform.getName());

        if (scanner == null)
            throw new IllegalStateException("No scanner implementation found for type: " + platform.getName());

        return scanner;
    }

}
