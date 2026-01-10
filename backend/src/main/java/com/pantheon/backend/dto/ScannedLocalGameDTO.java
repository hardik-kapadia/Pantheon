package com.pantheon.backend.dto;

import com.pantheon.backend.model.PlatformType;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ScannedLocalGameDTO(
        String title,
        String platformGameId,
        String platformName,
        PlatformType platformType,
        String installPath,
        boolean isInstalled,
        LocalDateTime lastPlayed,
        Integer playtimeMinutes
) {
}
