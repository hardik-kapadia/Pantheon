package com.pantheon.backend.core.notification.event.localscan;

import com.pantheon.backend.core.inventory.local.dto.ScannedLocalGameDTO;

import java.util.List;

public record LocalScanBatchEvent(
        String platformName,
        int gamesFound,
        List<ScannedLocalGameDTO> games
) implements LocalScanEvent {

    public LocalScanBatchEvent(String platformName, List<ScannedLocalGameDTO> games) {
        this(platformName, games.size(), games);
    }
}
