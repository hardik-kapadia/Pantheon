package com.pantheon.backend.core.notification.event.localscan;

import java.util.List;

public record LocalScanCompletedEvent(
        String platformName,
        int finalCount,
        boolean success,
        int failedPathsCount,
        List<String> failedPaths
) implements LocalScanEvent {
}
