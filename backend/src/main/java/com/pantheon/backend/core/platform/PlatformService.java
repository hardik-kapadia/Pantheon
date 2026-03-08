package com.pantheon.backend.core.platform;

import com.pantheon.backend.core.library.utils.ScannerUtil;
import com.pantheon.backend.core.platform.dto.PlatformInitialSetupDTO;
import com.pantheon.backend.core.platform.io.GetAllPlatformResponse;
import com.pantheon.backend.core.platform.io.GetPlatformByNameRequest;
import com.pantheon.backend.core.platform.io.GetPlatformByNameResponse;
import com.pantheon.backend.core.platform.model.Platform;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlatformService {

    private final PlatformRepository platformRepository;
    private final ScannerUtil scannerUtil;

    public GetAllPlatformResponse getAllPlatforms() {
        return GetAllPlatformResponse.Success.builder()
                .platforms(new HashSet<>(platformRepository.findAll()))
                .build();
    }

    public GetPlatformByNameResponse getPlatform(GetPlatformByNameRequest request) {

        String name = request.name();

        return platformRepository.findByName(name)
                .<GetPlatformByNameResponse>map(p -> GetPlatformByNameResponse.Success.builder()
                        .platform(p)
                        .build())
                .orElseGet(() -> GetPlatformByNameResponse.InvalidInput.builder()
                        .message("Platform with name %s not found".formatted(name))
                        .build());
    }

    public Platform setupPlatform(PlatformInitialSetupDTO platformInitialSetupDTO) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not yet implemented");
    }


}
