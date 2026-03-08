package com.pantheon.backend.core.platform.io;

import com.pantheon.backend.core.platform.model.Platform;
import lombok.Builder;

public sealed interface GetPlatformByNameResponse permits GetPlatformByNameResponse.InvalidInput, GetPlatformByNameResponse.Success {

    @Builder
    record Success(Platform platform) implements GetPlatformByNameResponse {
    }

    @Builder
    record InvalidInput(String message) implements GetPlatformByNameResponse {
    }

}
