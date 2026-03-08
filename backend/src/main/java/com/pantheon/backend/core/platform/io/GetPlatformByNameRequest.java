package com.pantheon.backend.core.platform.io;

import lombok.Builder;

@Builder
public record GetPlatformByNameRequest(String name) {
}
