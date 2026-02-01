package com.pantheon.backend.core.notification;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface NotificationService {

    SseEmitter subscribe();

    void broadcast(String eventName, Object payload);

}
