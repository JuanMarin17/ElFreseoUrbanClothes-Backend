package com.api.Reviews.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class NotificationService {

    private final Map<UUID, CopyOnWriteArrayList<SseEmitter>> storeEmitters = new ConcurrentHashMap<>();

    public SseEmitter subscribeStore(UUID storeId) {
        SseEmitter emitter = new SseEmitter(0L);
        storeEmitters.computeIfAbsent(storeId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        Runnable cleanup = () -> removeEmitter(storeId, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> removeEmitter(storeId, emitter));
        return emitter;
    }

    public void notifyStore(UUID storeId, String eventName, Object data) {
        CopyOnWriteArrayList<SseEmitter> emitters = storeEmitters.get(storeId);
        if (emitters == null || emitters.isEmpty()) return;
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                removeEmitter(storeId, emitter);
            }
        }
    }

    private void removeEmitter(UUID storeId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list = storeEmitters.get(storeId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) storeEmitters.remove(storeId);
        }
    }
}
