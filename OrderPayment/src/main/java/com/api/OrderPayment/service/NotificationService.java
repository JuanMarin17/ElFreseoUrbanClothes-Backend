package com.api.OrderPayment.service;

import com.api.OrderPayment.dto.notification.NotificationEvent;
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

    // Streams por storeId → admin/owner
    private final Map<UUID, CopyOnWriteArrayList<SseEmitter>> storeEmitters = new ConcurrentHashMap<>();
    // Streams por userId → cliente
    private final Map<UUID, CopyOnWriteArrayList<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    public SseEmitter subscribeStore(UUID storeId) {
        return subscribe(storeEmitters, storeId);
    }

    public SseEmitter subscribeUser(UUID userId) {
        return subscribe(userEmitters, userId);
    }

    public void notifyStore(UUID storeId, NotificationEvent event) {
        send(storeEmitters, storeId, event);
    }

    public void notifyUser(UUID userId, NotificationEvent event) {
        send(userEmitters, userId, event);
    }

    public void sendSessionAlert(UUID userId, String ip, String userAgent) {
        NotificationEvent event = NotificationEvent.builder()
                .type("SESSION_ALERT")
                .title("Nuevo inicio de sesión")
                .message("Se detectó un acceso a tu cuenta desde un nuevo dispositivo.")
                .data(Map.of(
                        "ip", ip != null ? ip : "Desconocida",
                        "device", userAgent != null ? userAgent : "Desconocido"))
                .build();
        send(userEmitters, userId, event);
    }

    private SseEmitter subscribe(Map<UUID, CopyOnWriteArrayList<SseEmitter>> map, UUID key) {
        SseEmitter emitter = new SseEmitter(0L);
        map.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(emitter);
        Runnable cleanup = () -> removeEmitter(map, key, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> removeEmitter(map, key, emitter));
        return emitter;
    }

    private void send(Map<UUID, CopyOnWriteArrayList<SseEmitter>> map, UUID key, NotificationEvent event) {
        CopyOnWriteArrayList<SseEmitter> emitters = map.get(key);
        if (emitters == null || emitters.isEmpty()) return;
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(event));
            } catch (IOException e) {
                removeEmitter(map, key, emitter);
            }
        }
    }

    private void removeEmitter(Map<UUID, CopyOnWriteArrayList<SseEmitter>> map, UUID key, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list = map.get(key);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) map.remove(key);
        }
    }
}
