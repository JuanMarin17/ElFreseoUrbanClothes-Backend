package com.api.OrderPayment.service;

import com.api.OrderPayment.dto.notification.NotificationEvent;
import com.api.OrderPayment.dto.notification.NotificationResponseDTO;
import com.api.OrderPayment.entity.Notification;
import com.api.OrderPayment.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

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
        persistAndNotifyUser(userId, event);
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
        persistAndNotifyUser(userId, event);
    }

    public void sendStoreStatusChanged(UUID userId, UUID storeId, String storeName, boolean isActive, String reason) {
        NotificationEvent event = isActive
                ? NotificationEvent.builder()
                        .type("STORE_ENABLED")
                        .title("Tu tienda fue habilitada")
                        .message(storeName + " fue habilitada nuevamente")
                        .data(Map.of("storeId", storeId))
                        .build()
                : NotificationEvent.builder()
                        .type("STORE_DISABLED")
                        .title("Tu tienda fue inhabilitada")
                        .message(storeName + " fue inhabilitada por un administrador")
                        .data(Map.of("storeId", storeId, "reason", reason != null ? reason : ""))
                        .build();
        persistAndNotifyUser(userId, event);
    }

    // ── Bandeja de notificaciones ─────────────────────────────────────────────

    public List<NotificationResponseDTO> getUserNotifications(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void markAsRead(UUID notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalse(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    // ── Internos ─────────────────────────────────────────────────────────────

    /**
     * Persiste la notificación para que quede en la bandeja del usuario aunque no
     * tenga el stream SSE abierto en este momento, y la envía en tiempo real si lo tiene.
     */
    @SuppressWarnings("unchecked")
    private void persistAndNotifyUser(UUID userId, NotificationEvent event) {
        Notification saved = notificationRepository.save(Notification.builder()
                .userId(userId)
                .type(event.getType())
                .title(event.getTitle())
                .message(event.getMessage())
                .data(event.getData() instanceof Map ? (Map<String, Object>) event.getData() : Map.of())
                .read(false)
                .build());

        event.setId(saved.getId());
        send(userEmitters, userId, event);
    }

    private NotificationResponseDTO toResponse(Notification n) {
        return NotificationResponseDTO.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .data(n.getData())
                .read(n.getRead())
                .createdAt(n.getCreatedAt())
                .build();
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
