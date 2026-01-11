package com.pantheon.backend.event.listener;

import com.pantheon.backend.event.localscan.LocalScanBatchEvent;
import com.pantheon.backend.event.localscan.LocalScanCompletedEvent;
import com.pantheon.backend.event.localscan.LocalScanErrorEvent;
import com.pantheon.backend.event.localscan.LocalScanStartedEvent;
import com.pantheon.backend.web.SsePubSub;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LibraryScanEventListener {

    private final SsePubSub ssePubSub;

    @EventListener
    public void onScanStarted(LocalScanStartedEvent event) {
        ssePubSub.broadcast("LOCAL_SCAN_STARTED", event);
    }

    @EventListener
    public void onScanBatch(LocalScanBatchEvent event) {
        ssePubSub.broadcast("LOCAL_SCAN_BATCH", event);
    }

    @EventListener
    public void onScanCompleted(LocalScanCompletedEvent event) {
        ssePubSub.broadcast("LOCAL_SCAN_COMPLETED", event);
    }

    @EventListener
    public void onScanFailed(LocalScanErrorEvent event) {
        ssePubSub.broadcast("LOCAL_SCAN_FAILED", event);
    }
}