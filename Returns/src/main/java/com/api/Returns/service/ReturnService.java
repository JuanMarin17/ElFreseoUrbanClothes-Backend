package com.api.Returns.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.Returns.client.OrderClient;
import com.api.Returns.dto.ApiResponseDTO;
import com.api.Returns.dto.ReturnItemResponseDTO;
import com.api.Returns.dto.ReturnRequestDTO;
import com.api.Returns.dto.ReturnResponseDTO;
import com.api.Returns.dto.UpdateReturnStatusDTO;
import com.api.Returns.entity.ReturnItem;
import com.api.Returns.entity.ReturnRequest;
import com.api.Returns.enums.ReturnStatus;
import com.api.Returns.exception.BadRequestException;
import com.api.Returns.exception.ReturnNotFoundException;
import com.api.Returns.exception.UnauthorizedException;
import com.api.Returns.repository.ReturnItemRepository;
import com.api.Returns.repository.ReturnRequestRepository;
import com.common_request_context_starter.context.RequestContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReturnService {

    private final ReturnRequestRepository returnRepository;
    private final ReturnItemRepository returnItemRepository;
    private final OrderClient orderClient;
    private final NotificationService notificationService;

    // ── Crear solicitud de devolución ─────────────────────────────────────────
    @Transactional
    public ReturnResponseDTO createReturn(ReturnRequestDTO dto) {
        UUID userId = getUserIdFromHeader();
        UUID storeId = getStoreIdFromHeader();

        if (dto.getOrderId() == null)
            throw new BadRequestException("El orderId es obligatorio");

        if (dto.getReason() == null || dto.getReason().isBlank())
            throw new BadRequestException("El motivo de devolución es obligatorio");

        if (dto.getItems() == null || dto.getItems().isEmpty())
            throw new BadRequestException("Debe incluir al menos un ítem para devolver");

        if (!orderClient.existsOrder(storeId, dto.getOrderId()))
            throw new BadRequestException("La orden no existe");

        if (returnRepository.existsByOrderIdAndUserId(dto.getOrderId(), userId))
            throw new BadRequestException("Ya existe una solicitud de devolución para esta orden");

        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setOrderId(dto.getOrderId());
        returnRequest.setUserId(userId);
        returnRequest.setStoreId(storeId);
        returnRequest.setReason(dto.getReason());

        ReturnRequest saved = returnRepository.save(returnRequest);

        List<ReturnItem> items = dto.getItems().stream().map(i -> {
            if (i.getQuantity() == null || i.getQuantity() <= 0)
                throw new BadRequestException("La cantidad de cada ítem debe ser mayor a 0");

            ReturnItem item = new ReturnItem();
            item.setReturnId(saved.getReturnId());
            item.setVariantId(i.getVariantId());
            item.setQuantity(i.getQuantity());
            return item;
        }).toList();

        returnItemRepository.saveAll(items);

        try {
            notificationService.notifyStore(storeId, "new-return", Map.of(
                    "orderId", saved.getOrderId(),
                    "reason", saved.getReason(),
                    "returnId", saved.getReturnId()));
        } catch (Exception e) {
            // notificación no bloquea la respuesta
        }

        return toResponse(saved, items);
    }

    // ── Mis solicitudes de devolución ─────────────────────────────────────────
    public List<ReturnResponseDTO> getMyReturns() {
        UUID userId = getUserIdFromHeader();
        return returnRepository.findByUserId(userId)
                .stream().map(r -> toResponse(r,
                        returnItemRepository.findByReturnId(r.getReturnId())))
                .toList();
    }

    // ── Ver todas las devoluciones de la tienda (ADMIN/OWNER) ─────────────────
    public List<ReturnResponseDTO> getReturnsByStore() {
        validateAdminOrOwner();
        UUID storeId = getStoreIdFromHeader();
        return returnRepository.findByStoreId(storeId)
                .stream().map(r -> toResponse(r,
                        returnItemRepository.findByReturnId(r.getReturnId())))
                .toList();
    }

    // ── Ver detalle de una devolución ─────────────────────────────────────────
    public ReturnResponseDTO getReturnById(UUID returnId) {
        UUID userId = getUserIdFromHeader();
        String role = RequestContext.getHeader("X-User-Role");

        ReturnRequest returnRequest = returnRepository.findById(returnId)
                .orElseThrow(() -> new ReturnNotFoundException(
                        "Solicitud de devolución no encontrada con id: " + returnId));

        if (!returnRequest.getUserId().equals(userId) &&
                !"ADMIN".equals(role) && !"OWNER".equals(role))
            throw new UnauthorizedException("No tienes permisos para ver esta solicitud");

        return toResponse(returnRequest,
                returnItemRepository.findByReturnId(returnId));
    }

    // ── Actualizar estado (ADMIN/OWNER) ───────────────────────────────────────
    @Transactional
    public ReturnResponseDTO updateReturnStatus(UUID returnId, UpdateReturnStatusDTO dto) {
        validateAdminOrOwner();

        if (dto.getStatus() == null)
            throw new BadRequestException("El estado es obligatorio");

        ReturnRequest returnRequest = returnRepository.findById(returnId)
                .orElseThrow(() -> new ReturnNotFoundException(
                        "Solicitud de devolución no encontrada con id: " + returnId));

        if (returnRequest.getStatus() == ReturnStatus.COMPLETED ||
                returnRequest.getStatus() == ReturnStatus.REJECTED)
            throw new BadRequestException("No se puede cambiar el estado de una solicitud " +
                    returnRequest.getStatus().name().toLowerCase());

        returnRequest.setStatus(dto.getStatus());
        ReturnRequest saved = returnRepository.save(returnRequest);

        return toResponse(saved, returnItemRepository.findByReturnId(returnId));
    }

    // ── Cancelar solicitud (solo el usuario que la creó, solo si está PENDING) ─
    @Transactional
    public ApiResponseDTO cancelReturn(UUID returnId) {
        UUID userId = getUserIdFromHeader();

        ReturnRequest returnRequest = returnRepository.findById(returnId)
                .orElseThrow(() -> new ReturnNotFoundException(
                        "Solicitud de devolución no encontrada con id: " + returnId));

        if (!returnRequest.getUserId().equals(userId))
            throw new UnauthorizedException("No tienes permisos para cancelar esta solicitud");

        if (returnRequest.getStatus() != ReturnStatus.PENDING)
            throw new BadRequestException("Solo se pueden cancelar solicitudes en estado PENDING");

        returnRepository.delete(returnRequest);

        ApiResponseDTO response = new ApiResponseDTO();
        response.setMessage("Solicitud de devolución cancelada correctamente");
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

    private UUID getStoreIdFromHeader() {
        String storeIdHeader = RequestContext.getHeader("X-Store-Id");
        if (storeIdHeader == null || storeIdHeader.isBlank())
            throw new BadRequestException("No se encontró el X-Store-Id en el header");
        try {
            return UUID.fromString(storeIdHeader);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Formato inválido del storeId");
        }
    }

    private void validateAdminOrOwner() {
        String role = RequestContext.getHeader("X-User-Role");
        if (!"ADMIN".equals(role) && !"OWNER".equals(role))
            throw new UnauthorizedException("Solo el ADMIN u OWNER pueden realizar esta acción");
    }

    // ── Mappers ───────────────────────────────────────────────────────────────
    private ReturnResponseDTO toResponse(ReturnRequest r, List<ReturnItem> items) {
        ReturnResponseDTO dto = new ReturnResponseDTO();
        dto.setReturnId(r.getReturnId());
        dto.setOrderId(r.getOrderId());
        dto.setUserId(r.getUserId());
        dto.setStoreId(r.getStoreId());
        dto.setReason(r.getReason());
        dto.setStatus(r.getStatus());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setItems(items.stream().map(i -> {
            ReturnItemResponseDTO itemDto = new ReturnItemResponseDTO();
            itemDto.setReturnItemId(i.getReturnItemId());
            itemDto.setVariantId(i.getVariantId());
            itemDto.setQuantity(i.getQuantity());
            itemDto.setCreatedAt(i.getCreatedAt());
            return itemDto;
        }).toList());
        return dto;
    }
}