package com.pantheon.backend.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class SsePubSub {

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {

        // Timeout: 0 = Infinite (or managed by server config)
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        emitters.add(emitter);
        log.info("New SSE client subscribed. Active clients: {}", emitters.size());
        return emitter;
    }

    public void broadcast(String eventName, Object payload) {
        if (emitters.isEmpty()) return;

        SseEmitter.SseEventBuilder event = SseEmitter.event()
                .name(eventName) // e.g., "SCAN_BATCH"
                .data(payload);

        // Send to all clients (Electron)
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(event);
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

}
