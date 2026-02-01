package com.pantheon.backend.core.notification.event.localscan;

public record LocalScanStartedEvent(
        String platformName,
        int totalGames
) implements LocalScanEvent {
}
