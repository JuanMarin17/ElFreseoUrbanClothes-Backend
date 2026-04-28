package com.apiSupport.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.apiSupport.dto.CreateTicketRequest;
import com.apiSupport.dto.TicketResponse;
import com.apiSupport.entity.SupportTicket;
import com.apiSupport.entity.TicketPriority;
import com.apiSupport.entity.TicketStatus;
import com.apiSupport.repository.SupportTicketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupportTicketService {

    private final SupportTicketRepository ticketRepository;

    public TicketResponse createTicket(CreateTicketRequest dto) {
        try {
            validateCreateTicket(dto);

            TicketPriority priority = parsePriority(dto.getPriority());

            SupportTicket ticket = SupportTicket.builder()
                    .userId(dto.getUserId())
                    .subject(dto.getSubject().trim())
                    .description(dto.getDescription().trim())
                    .status(TicketStatus.OPEN)
                    .priority(priority)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .closedAt(null)
                    .build();

            SupportTicket saved = ticketRepository.save(ticket);

            return mapToResponse(saved);

        } catch (RuntimeException e) {
            throw new RuntimeException("Error creating ticket: " + e.getMessage());
        }
    }

    public TicketResponse getTicketById(UUID ticketId) {
        try {
            if (ticketId == null) {
                throw new RuntimeException("ticketId is required");
            }

            SupportTicket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Ticket not found"));

            return mapToResponse(ticket);

        } catch (RuntimeException e) {
            throw new RuntimeException("Error getting ticket: " + e.getMessage());
        }
    }

    public List<TicketResponse> getTicketsByUser(UUID userId) {
        try {
            if (userId == null) {
                throw new RuntimeException("userId is required");
            }

            return ticketRepository.findByUserId(userId)
                    .stream()
                    .map(this::mapToResponse)
                    .toList();

        } catch (RuntimeException e) {
            throw new RuntimeException("Error listing tickets: " + e.getMessage());
        }
    }

    public TicketResponse updateTicketStatus(UUID ticketId, String status) {
        try {
            if (ticketId == null) {
                throw new RuntimeException("ticketId is required");
            }

            TicketStatus newStatus = parseStatus(status);

            SupportTicket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Ticket not found"));

            if (ticket.getStatus() == TicketStatus.CLOSED) {
                throw new RuntimeException("This ticket is CLOSED and cannot be modified");
            }

            ticket.setStatus(newStatus);
            ticket.setUpdatedAt(LocalDateTime.now());

            if (newStatus == TicketStatus.CLOSED) {
                ticket.setClosedAt(LocalDateTime.now());
            }

            SupportTicket updated = ticketRepository.save(ticket);

            return mapToResponse(updated);

        } catch (RuntimeException e) {
            throw new RuntimeException("Error updating ticket status: " + e.getMessage());
        }
    }

    public TicketResponse closeTicket(UUID ticketId) {
        return updateTicketStatus(ticketId, "CLOSED");
    }

    public void deleteTicket(UUID ticketId) {
        try {
            if (ticketId == null) {
                throw new RuntimeException("ticketId is required");
            }

            if (!ticketRepository.existsById(ticketId)) {
                throw new RuntimeException("Ticket not found");
            }

            ticketRepository.deleteById(ticketId);

        } catch (RuntimeException e) {
            throw new RuntimeException("Error deleting ticket: " + e.getMessage());
        }
    }

    // ==========================
    // PRIVATE METHODS
    // ==========================

    private void validateCreateTicket(CreateTicketRequest dto) {
        if (dto == null) {
            throw new RuntimeException("Request body is required");
        }

        if (dto.getUserId() == null) {
            throw new RuntimeException("userId is required");
        }

        if (dto.getSubject() == null || dto.getSubject().trim().isEmpty()) {
            throw new RuntimeException("subject is required");
        }

        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            throw new RuntimeException("description is required");
        }
    }

    private TicketPriority parsePriority(String priority) {
        if (priority == null || priority.trim().isEmpty()) {
            return TicketPriority.MEDIUM;
        }

        try {
            return TicketPriority.valueOf(priority.toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Invalid priority. Use LOW, MEDIUM, HIGH, URGENT");
        }
    }

    private TicketStatus parseStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new RuntimeException("status is required");
        }

        try {
            return TicketStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Invalid status. Use OPEN, IN_PROGRESS, RESOLVED, CLOSED");
        }
    }

    private TicketResponse mapToResponse(SupportTicket ticket) {
        return TicketResponse.builder()
                .ticketId(ticket.getTicketId())
                .userId(ticket.getUserId())
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .status(ticket.getStatus().name())
                .priority(ticket.getPriority().name())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .closedAt(ticket.getClosedAt())
                .build();
    }
}