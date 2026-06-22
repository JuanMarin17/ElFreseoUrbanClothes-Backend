package com.api.OrderPayment.controller;

import com.api.OrderPayment.dto.notification.NotificationResponseDTO;
import com.api.OrderPayment.dto.notification.SessionAlertRequestDTO;
import com.api.OrderPayment.dto.notification.StoreStatusChangedRequestDTO;
import com.api.OrderPayment.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** Stream SSE para admin/owner: órdenes nuevas */
    @GetMapping("/stores/{storeId}/notifications/admin/stream")
    public SseEmitter storeStream(@PathVariable UUID storeId) {
        return notificationService.subscribeStore(storeId);
    }

    /** Stream SSE para el usuario: cambios de estado, pagos y alertas de sesión */
    @GetMapping("/notifications/user/{userId}/stream")
    public SseEmitter userStream(@PathVariable UUID userId) {
        return notificationService.subscribeUser(userId);
    }

    /** Llamada interna genérica: cualquier microservicio puede notificar al usuario */
    @PostMapping("/internal/notifications/user/{userId}")
    public ResponseEntity<Void> notifyUser(@PathVariable UUID userId,
                                           @RequestBody Map<String, Object> payload) {
        notificationService.notifyUser(userId, com.api.OrderPayment.dto.notification.NotificationEvent.builder()
                .type((String) payload.get("type"))
                .title((String) payload.get("title"))
                .message((String) payload.get("message"))
                .data(payload.get("data"))
                .build());
        return ResponseEntity.ok().build();
    }

    /** Llamada interna genérica: cualquier microservicio puede notificar al admin/owner de la tienda */
    @PostMapping("/internal/notifications/store/{storeId}")
    public ResponseEntity<Void> notifyStore(@PathVariable UUID storeId,
                                            @RequestBody Map<String, Object> payload) {
        notificationService.notifyStore(storeId, com.api.OrderPayment.dto.notification.NotificationEvent.builder()
                .type((String) payload.get("type"))
                .title((String) payload.get("title"))
                .message((String) payload.get("message"))
                .data(payload.get("data"))
                .build());
        return ResponseEntity.ok().build();
    }

    /** Llamada interna desde Auth: notifica al usuario que se abrió una nueva sesión */
    @PostMapping("/internal/notifications/user/{userId}/session-alert")
    public ResponseEntity<Void> sessionAlert(@PathVariable UUID userId,
                                             @RequestBody SessionAlertRequestDTO dto) {
        notificationService.sendSessionAlert(userId, dto.getIp(), dto.getUserAgent());
        return ResponseEntity.ok().build();
    }

    /** Llamada interna desde Store: notifica al owner que su tienda fue inhabilitada/habilitada */
    @PostMapping("/internal/notifications/user/{userId}/store-status")
    public ResponseEntity<Void> storeStatusChanged(@PathVariable UUID userId,
                                                    @RequestBody StoreStatusChangedRequestDTO dto) {
        notificationService.sendStoreStatusChanged(
                userId, dto.getStoreId(), dto.getStoreName(), Boolean.TRUE.equals(dto.getIsActive()), dto.getReason());
        return ResponseEntity.ok().build();
    }

    /** Bandeja de notificaciones: historial completo del usuario, leídas y no leídas */
    @GetMapping("/notifications/user/{userId}")
    public ResponseEntity<List<NotificationResponseDTO>> getUserNotifications(@PathVariable UUID userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    /** Marcar una notificación puntual como leída */
    @PatchMapping("/notifications/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    /** Marcar todas las notificaciones del usuario como leídas */
    @PatchMapping("/notifications/user/{userId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable UUID userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
}
