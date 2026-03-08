package com.pantheon.backend.core.platform.io;

import com.pantheon.backend.core.platform.model.Platform;
import lombok.Builder;

import java.util.Set;

public sealed interface GetAllPlatformResponse permits GetAllPlatformResponse.Success {

    @Builder
    record Success(Set<Platform> platforms) implements GetAllPlatformResponse {
    }
}
