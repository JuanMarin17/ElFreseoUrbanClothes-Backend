package com.api.Support.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.Support.dto.ApiResponseDTO;
import com.api.Support.dto.MessageRequestDTO;
import com.api.Support.dto.MessageResponseDTO;
import com.api.Support.dto.TicketRequestDTO;
import com.api.Support.dto.TicketResponseDTO;
import com.api.Support.service.SupportService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/support")
@RequiredArgsConstructor
public class SupportController {

    private final SupportService supportService;

    // Crear ticket
    @PostMapping("/tickets")
    public ResponseEntity<TicketResponseDTO> createTicket(
            @Valid @RequestBody TicketRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supportService.createTicket(dto));
    }

    // Mis tickets
    @GetMapping("/tickets/me")
    public ResponseEntity<List<TicketResponseDTO>> getMyTickets() {
        return ResponseEntity.ok(supportService.getMyTickets());
    }

    // Todos los tickets (solo OWNER)
    @GetMapping("/tickets")
    public ResponseEntity<List<TicketResponseDTO>> getAllTickets() {
        return ResponseEntity.ok(supportService.getAllTickets());
    }

    // Obtener ticket por ID
    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<TicketResponseDTO> getTicketById(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(supportService.getTicketById(ticketId));
    }

    // Responder ticket (solo OWNER)
    @PostMapping("/tickets/{ticketId}/reply")
    public ResponseEntity<MessageResponseDTO> replyTicket(
            @PathVariable UUID ticketId,
            @Valid @RequestBody MessageRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supportService.replyTicket(ticketId, dto));
    }

    // Mensajes de un ticket
    @GetMapping("/tickets/{ticketId}/messages")
    public ResponseEntity<List<MessageResponseDTO>> getMessages(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(supportService.getMessagesByTicket(ticketId));
    }

    // Cerrar ticket (solo OWNER)
    @PatchMapping("/tickets/{ticketId}/close")
    public ResponseEntity<ApiResponseDTO> closeTicket(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(supportService.closeTicket(ticketId));
    }
}