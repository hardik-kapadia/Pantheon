package com.pantheon.backend.core.notification.event.localscan;

public record LocalScanErrorEvent(String platformName, String errorMessage) implements LocalScanEvent {
}
