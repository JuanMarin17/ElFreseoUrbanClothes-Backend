package com.user.api.user.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.user.api.user.dto.UserSessionResponseDTO;
import com.user.api.user.service.UserSessionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/sessions")
public class UserSessionController {

    private final UserSessionService userSessionService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<UserSessionResponseDTO>> getSessions(@PathVariable UUID userId) {
        return ResponseEntity.ok(userSessionService.getActiveSessions(userId));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deactivateSession(@PathVariable UUID sessionId) {
        userSessionService.deactivateSession(sessionId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deactivateAllSessions() {
        userSessionService.deactivateAllSessions();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /** Endpoint interno — llamado por el Gateway para verificar si una sesión sigue activa. Sin JWT. */
    @GetMapping("/internal/{sessionId}/active")
    public ResponseEntity<Void> isSessionActive(@PathVariable UUID sessionId) {
        return userSessionService.isSessionActive(sessionId)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }
}
