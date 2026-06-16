package com.api.OrderPayment.controller;

import com.api.OrderPayment.dto.notification.SessionAlertRequestDTO;
import com.api.OrderPayment.service.NotificationService;
import com.api.OrderPayment.util.HeaderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final HeaderUtil headerUtil;

    /** Stream SSE para admin/owner: órdenes nuevas */
    @GetMapping("/stores/{storeId}/notifications/admin/stream")
    public SseEmitter storeStream(@PathVariable UUID storeId) {
        return notificationService.subscribeStore(storeId);
    }

    /** Stream SSE para el usuario autenticado: cambios de estado y pagos */
    @GetMapping("/notifications/user/stream")
    public SseEmitter userStream() {
        UUID userId = headerUtil.requireUserId();
        return notificationService.subscribeUser(userId);
    }

    /** Llamada interna desde Auth: notifica al usuario que se abrió una nueva sesión */
    @PostMapping("/internal/notifications/user/{userId}/session-alert")
    public ResponseEntity<Void> sessionAlert(@PathVariable UUID userId,
                                             @RequestBody SessionAlertRequestDTO dto) {
        notificationService.sendSessionAlert(userId, dto.getIp(), dto.getUserAgent());
        return ResponseEntity.ok().build();
    }
}
