package com.api.Support.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.Support.client.NotificationClient;
import com.api.Support.dto.ApiResponseDTO;
import com.api.Support.dto.MessageRequestDTO;
import com.api.Support.dto.MessageResponseDTO;
import com.api.Support.dto.TicketRequestDTO;
import com.api.Support.dto.TicketResponseDTO;
import com.api.Support.entity.SupportMessage;
import com.api.Support.entity.SupportTicket;
import com.api.Support.enums.TicketStatus;
import com.api.Support.exception.BadRequestException;
import com.api.Support.exception.TicketNotFoundException;
import com.api.Support.exception.UnauthorizedException;
import com.api.Support.repository.SupportMessageRepository;
import com.api.Support.repository.SupportTicketRepository;
import com.common_request_context_starter.context.RequestContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupportService {

    private final SupportTicketRepository ticketRepository;
    private final SupportMessageRepository messageRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final NotificationClient notificationClient;

    // ── Crear ticket ─────────────────────────────────────────────────────────
    public TicketResponseDTO createTicket(TicketRequestDTO dto) {
        UUID userId = getUserIdFromHeader();
        String userEmail = getUserEmailFromHeader();

        SupportTicket ticket = new SupportTicket();
        ticket.setUserId(userId);
        ticket.setUserEmail(userEmail);
        ticket.setSubject(dto.getSubject());
        ticket.setStoreId(dto.getStoreId());

        SupportTicket saved = ticketRepository.save(ticket);
        emailService.sendTicketCreatedEmail(userEmail, saved.getSubject(), saved.getTicketId());

        if (saved.getStoreId() != null) {
            try {
                notificationService.notifyStore(saved.getStoreId(), "new-ticket", Map.of(
                        "ticketId", saved.getTicketId(),
                        "subject", saved.getSubject(),
                        "userEmail", userEmail));
            } catch (Exception e) {
                // notificación no bloquea la respuesta
            }
        }

        return toTicketResponse(saved);
    }

    // ── Obtener mis tickets (usuario autenticado) ─────────────────────────────
    public List<TicketResponseDTO> getMyTickets() {
        UUID userId = getUserIdFromHeader();

        return ticketRepository.findByUserId(userId)
                .stream()
                .map(this::toTicketResponse)
                .toList();
    }

    // ── Obtener todos los tickets (solo OWNER) ────────────────────────────────
    public Page<TicketResponseDTO> getAllTickets(Pageable pageable) {
        validateSuperAdmin();

        return ticketRepository.findAll(pageable)
                .map(this::toTicketResponse);
    }

    // ── Obtener ticket por ID ─────────────────────────────────────────────────
    public TicketResponseDTO getTicketById(UUID ticketId) {
        UUID userId = getUserIdFromHeader();
        String role = RequestContext.getHeader("X-User-Role");

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket no encontrado con id: " + ticketId));

        // Solo el dueño del ticket o OWNER puede verlo
        if (!ticket.getUserId().equals(userId) && !"OWNER".equals(role))
            throw new UnauthorizedException("No tienes permisos para ver este ticket");

        return toTicketResponse(ticket);
    }

    // ── Responder ticket (solo OWNER) ─────────────────────────────────────────
    @Transactional
    public MessageResponseDTO replyTicket(UUID ticketId, MessageRequestDTO dto) {
        validateSuperAdmin();

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket no encontrado con id: " + ticketId));

        if (ticket.getStatus() == TicketStatus.CLOSED)
            throw new BadRequestException("No se puede responder un ticket cerrado");

        UUID senderId = getUserIdFromHeader();

        SupportMessage message = new SupportMessage();
        message.setTicketId(ticketId);
        message.setSenderId(senderId);
        message.setMessage(dto.getMessage());

        // Al responder cambia a IN_PROGRESS
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticketRepository.save(ticket);

        SupportMessage saved = messageRepository.save(message);
        if (ticket.getUserEmail() != null) {
            emailService.sendTicketRepliedEmail(ticket.getUserEmail(), ticket.getSubject(), ticketId, dto.getMessage());
        }
        try {
            notificationClient.notifyUser(
                    ticket.getUserId(),
                    "SUPPORT_TICKET_REPLIED",
                    "Respuesta en tu ticket",
                    "Tu ticket \"" + ticket.getSubject() + "\" ha recibido una respuesta.");
        } catch (Exception e) {
            log.warn("No se pudo enviar notificación SSE de respuesta a ticket {}: {}", ticketId, e.getMessage());
        }
        return toMessageResponse(saved);
    }

    // ── Obtener mensajes de un ticket ─────────────────────────────────────────
    public List<MessageResponseDTO> getMessagesByTicket(UUID ticketId) {
        UUID userId = getUserIdFromHeader();
        String role = RequestContext.getHeader("X-User-Role");

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket no encontrado con id: " + ticketId));

        if (!ticket.getUserId().equals(userId) && !"OWNER".equals(role))
            throw new UnauthorizedException("No tienes permisos para ver los mensajes de este ticket");

        return messageRepository.findByTicketId(ticketId)
                .stream()
                .map(this::toMessageResponse)
                .toList();
    }

    // ── Cerrar ticket (solo OWNER) ────────────────────────────────────────────
    @Transactional
    public ApiResponseDTO closeTicket(UUID ticketId) {
        validateSuperAdmin();

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket no encontrado con id: " + ticketId));

        if (ticket.getStatus() == TicketStatus.CLOSED)
            throw new BadRequestException("El ticket ya está cerrado");

        ticket.setStatus(TicketStatus.CLOSED);
        ticketRepository.save(ticket);

        if (ticket.getUserEmail() != null) {
            emailService.sendTicketClosedEmail(ticket.getUserEmail(), ticket.getSubject(), ticketId);
        }
        try {
            notificationClient.notifyUser(
                    ticket.getUserId(),
                    "SUPPORT_TICKET_CLOSED",
                    "Ticket cerrado",
                    "Tu ticket \"" + ticket.getSubject() + "\" ha sido cerrado.");
        } catch (Exception e) {
            log.warn("No se pudo enviar notificación SSE de cierre a ticket {}: {}", ticketId, e.getMessage());
        }

        ApiResponseDTO response = new ApiResponseDTO();
        response.setMessage("Ticket cerrado correctamente");
        response.setStatus(200);
        return response;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private UUID getUserIdFromHeader() {
        String userIdHeader = RequestContext.getHeader("x-user-id");
        if (userIdHeader == null || userIdHeader.isBlank())
            throw new UnauthorizedException("Usuario no autenticado");
        try {
            return UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Formato inválido del userId");
        }
    }

    private String getUserEmailFromHeader() {
        String email = RequestContext.getHeader("x-user-email");
        if (email == null || email.isBlank())
            throw new UnauthorizedException("No se pudo obtener el email del usuario autenticado");
        return email;
    }

    private void validateSuperAdmin() {
        String role = RequestContext.getHeader("x-user-role");
        if (!"SUPERADMIN".equals(role))
            throw new UnauthorizedException("Solo el SUPERADMIN puede realizar esta acción");
    }

    // ── Mappers ───────────────────────────────────────────────────────────────
    private TicketResponseDTO toTicketResponse(SupportTicket t) {
        TicketResponseDTO dto = new TicketResponseDTO();
        dto.setTicketId(t.getTicketId());
        dto.setUserId(t.getUserId());
        dto.setSubject(t.getSubject());
        dto.setStatus(t.getStatus());
        dto.setCreatedAt(t.getCreatedAt());
        return dto;
    }

    private MessageResponseDTO toMessageResponse(SupportMessage m) {
        MessageResponseDTO dto = new MessageResponseDTO();
        dto.setMessageId(m.getMessageId());
        dto.setTicketId(m.getTicketId());
        dto.setSenderId(m.getSenderId());
        dto.setMessage(m.getMessage());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }
}