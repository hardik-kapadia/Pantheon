package com.pantheon.backend.event.localscan;

public record LocalScanErrorEvent(String platformName, String errorMessage) implements LocalScanEvent {
}
