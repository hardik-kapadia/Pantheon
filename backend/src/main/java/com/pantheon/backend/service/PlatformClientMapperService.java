package com.pantheon.backend.service;

import com.pantheon.backend.client.LocalGameLibraryClient;
import com.pantheon.backend.model.Platform;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PlatformClientMapperService {


    private final Map<String, LocalGameLibraryClient> localGameLibraryClientMap;

    @Autowired
    public PlatformClientMapperService(List<LocalGameLibraryClient> localGameLibraryClients) {
        this.localGameLibraryClientMap = localGameLibraryClients.stream()
                .collect(Collectors.toMap(com.pantheon.backend.client.LocalGameLibraryClient::getPlatformName, Function.identity()));
    }

    public LocalGameLibraryClient getScanner(@NonNull Platform platform) throws IllegalStateException {

        LocalGameLibraryClient scanner = localGameLibraryClientMap.get(platform.getName());

        if (scanner == null) {
            log.error("{}: No LibraryClient found ", platform.getName());
            throw new IllegalStateException("No scanner implementation found for type: " + platform.getName());
        }

        return scanner;
    }

}
