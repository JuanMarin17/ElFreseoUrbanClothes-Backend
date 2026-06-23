package com.api.Returns.controller;

import com.api.Returns.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** Stream SSE para admin/owner: nuevas solicitudes de devolución */
    @GetMapping("/stores/{storeId}/returns/notifications/stream")
    public SseEmitter storeStream(@PathVariable UUID storeId) {
        return notificationService.subscribeStore(storeId);
    }
}
