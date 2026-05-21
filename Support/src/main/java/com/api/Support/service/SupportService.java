package com.api.Support.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
@RequiredArgsConstructor
public class SupportService {

    private final SupportTicketRepository ticketRepository;
    private final SupportMessageRepository messageRepository;

    // ── Crear ticket ─────────────────────────────────────────────────────────
    public TicketResponseDTO createTicket(TicketRequestDTO dto) {
        UUID userId = getUserIdFromHeader();

        SupportTicket ticket = new SupportTicket();
        ticket.setUserId(userId);
        ticket.setSubject(dto.getSubject());

        return toTicketResponse(ticketRepository.save(ticket));
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
    public List<TicketResponseDTO> getAllTickets() {
        validateOwner();

        return ticketRepository.findAll()
                .stream()
                .map(this::toTicketResponse)
                .toList();
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
        validateOwner();

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

        return toMessageResponse(messageRepository.save(message));
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
        validateOwner();

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket no encontrado con id: " + ticketId));

        if (ticket.getStatus() == TicketStatus.CLOSED)
            throw new BadRequestException("El ticket ya está cerrado");

        ticket.setStatus(TicketStatus.CLOSED);
        ticketRepository.save(ticket);

        ApiResponseDTO response = new ApiResponseDTO();
        response.setMessage("Ticket cerrado correctamente");
        response.setStatus(200);
        return response;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private UUID getUserIdFromHeader() {
        String userIdHeader = RequestContext.getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.isBlank())
            throw new UnauthorizedException("Usuario no autenticado");
        try {
            return UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Formato inválido del userId");
        }
    }

    private void validateOwner() {
        String role = RequestContext.getHeader("X-User-Role");
        if (!"OWNER".equals(role))
            throw new UnauthorizedException("Solo el OWNER puede realizar esta acción");
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