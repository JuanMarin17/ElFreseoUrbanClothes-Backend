package com.api.Reviews.controller;

import com.api.Reviews.service.NotificationService;
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

    /** Stream SSE para admin/owner: comentarios/reseñas nuevas */
    @GetMapping("/stores/{storeId}/reviews/notifications/stream")
    public SseEmitter storeStream(@PathVariable UUID storeId) {
        return notificationService.subscribeStore(storeId);
    }
}
