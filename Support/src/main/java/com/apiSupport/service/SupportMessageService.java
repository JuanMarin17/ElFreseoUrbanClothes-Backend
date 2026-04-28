package com.apiSupport.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.apiSupport.dto.MessageRequest;
import com.apiSupport.dto.MessageResponse;
import com.apiSupport.entity.SupportMessage;
import com.apiSupport.entity.SupportTicket;
import com.apiSupport.entity.TicketStatus;
import com.apiSupport.repository.SupportMessageRepository;
import com.apiSupport.repository.SupportTicketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupportMessageService {

    private final SupportMessageRepository messageRepository;
    private final SupportTicketRepository ticketRepository;

    public MessageResponse addMessage(UUID ticketId, MessageRequest dto) {
        try {
            validateAddMessage(ticketId, dto);

            SupportTicket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Ticket not found"));

            if (ticket.getStatus() == TicketStatus.CLOSED) {
                throw new RuntimeException("Cannot add messages to a CLOSED ticket");
            }

            SupportMessage message = SupportMessage.builder()
                    .ticket(ticket)
                    .senderId(dto.getSenderId())
                    .message(dto.getMessage().trim())
                    .createdAt(LocalDateTime.now())
                    .build();

            SupportMessage saved = messageRepository.save(message);

            if (ticket.getStatus() == TicketStatus.OPEN) {
                ticket.setStatus(TicketStatus.IN_PROGRESS);
                ticket.setUpdatedAt(LocalDateTime.now());
                ticketRepository.save(ticket);
            }

            return mapToResponse(saved);

        } catch (RuntimeException e) {
            throw new RuntimeException("Error adding message: " + e.getMessage());
        }
    }

    public List<MessageResponse> getMessagesByTicket(UUID ticketId) {
        try {
            if (ticketId == null) {
                throw new RuntimeException("ticketId is required");
            }

            return messageRepository.findByTicket_TicketId(ticketId)
                    .stream()
                    .map(this::mapToResponse)
                    .toList();

        } catch (RuntimeException e) {
            throw new RuntimeException("Error listing messages: " + e.getMessage());
        }
    }

    // ==========================
    // PRIVATE METHODS
    // ==========================

    private void validateAddMessage(UUID ticketId, MessageRequest dto) {
        if (ticketId == null) {
            throw new RuntimeException("ticketId is required");
        }

        if (dto == null) {
            throw new RuntimeException("Request body is required");
        }

        if (dto.getSenderId() == null) {
            throw new RuntimeException("senderId is required");
        }

        if (dto.getMessage() == null || dto.getMessage().trim().isEmpty()) {
            throw new RuntimeException("message is required");
        }
    }

    private MessageResponse mapToResponse(SupportMessage message) {
        return MessageResponse.builder()
                .messageId(message.getMessageId())
                .ticketId(message.getTicket().getTicketId())
                .senderId(message.getSenderId())
                .message(message.getMessage())
                .createdAt(message.getCreatedAt())
                .build();
    }
}