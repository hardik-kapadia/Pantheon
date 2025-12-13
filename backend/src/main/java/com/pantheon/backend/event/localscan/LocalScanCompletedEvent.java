package com.pantheon.backend.event.localscan;

public record LocalScanCompletedEvent(
        String platformName,
        int finalCount,
        boolean success
) {
}
