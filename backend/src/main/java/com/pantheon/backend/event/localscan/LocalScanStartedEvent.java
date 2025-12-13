package com.pantheon.backend.event.localscan;

public record LocalScanStartedEvent(
        String platformName,
        int totalGames
) {
}
