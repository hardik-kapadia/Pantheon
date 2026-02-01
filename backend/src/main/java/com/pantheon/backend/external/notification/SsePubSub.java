package com.pantheon.backend.external.notification;

import com.pantheon.backend.core.notification.NotificationService;
import com.pantheon.backend.core.notification.event.localscan.LocalScanEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class SsePubSub implements NotificationService {

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    private final Long sseTimeout;

    public SsePubSub(@Qualifier("sseTimeout") Long sseTimeout) {
        this.sseTimeout = sseTimeout;
    }

    public SseEmitter subscribe() {

        SseEmitter emitter = new SseEmitter(sseTimeout);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((_) -> emitters.remove(emitter));

        emitters.add(emitter);
        log.info("New SSE client subscribed. Active clients: {}", emitters.size());
        return emitter;
    }

    public void broadcast(String eventName, Object payload) {
        if (emitters.isEmpty()) return;

        SseEmitter.SseEventBuilder event = SseEmitter.event()
                .name(eventName)
                .data(payload);

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(event);
            } catch (IOException e) {
                emitters.remove(emitter);

                if (payload instanceof LocalScanEvent localScanEvent) {
                    log.error("Failed to broadcast to emitter for {}: {}", localScanEvent.platformName(), eventName);
                } else {
                    log.error("Failed to broadcast {} ", eventName, e);
                }
            }
        }
    }

}
