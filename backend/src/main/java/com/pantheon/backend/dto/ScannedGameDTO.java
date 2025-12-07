package com.pantheon.backend.dto;

import com.pantheon.backend.model.PlatformType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class ScannedGameDTO {

    String title;

    String platformGameId;

    String platformName;

    PlatformType platformType;

    String installPath;

    boolean isInstalled;

    LocalDateTime lastPlayed;

    Integer playtimeMinutes;

}
