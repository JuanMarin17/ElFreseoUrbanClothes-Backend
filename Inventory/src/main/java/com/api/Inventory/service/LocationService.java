package com.api.Inventory.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.api.Inventory.dto.ApiResponseDTO;
import com.api.Inventory.dto.LocationRequestDTO;
import com.api.Inventory.dto.LocationResponseDTO;
import com.api.Inventory.entity.Location;
import com.api.Inventory.exception.BadRequestException;
import com.api.Inventory.exception.InventoryNotFoundException;
import com.api.Inventory.exception.UnauthorizedException;
import com.api.Inventory.repository.LocationRepository;
import com.common_request_context_starter.context.RequestContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    // ── Crear ubicación ───────────────────────────────────────────────────────
    public LocationResponseDTO createLocation(LocationRequestDTO dto) {
        validateAdmin();
        UUID storeId = getStoreIdFromHeader();

        if (dto.getName() == null || dto.getName().isBlank())
            throw new BadRequestException("El nombre de la ubicación es obligatorio");

        Location location = new Location();
        location.setName(dto.getName());
        location.setStoreId(storeId);

        return toLocationResponse(locationRepository.save(location));
    }

    // ── Obtener ubicaciones de la tienda ──────────────────────────────────────
    public List<LocationResponseDTO> getLocationsByStore() {
        UUID storeId = getStoreIdFromHeader();
        return locationRepository.findByStoreId(storeId)
                .stream().map(this::toLocationResponse).toList();
    }

    // ── Eliminar ubicación ────────────────────────────────────────────────────
    public ApiResponseDTO deleteLocation(UUID locationId) {
        validateAdmin();

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new InventoryNotFoundException("Ubicación no encontrada con id: " + locationId));

        locationRepository.delete(location);

        ApiResponseDTO response = new ApiResponseDTO();
        response.setMessage("Ubicación eliminada correctamente");
        response.setStatus(200);
        return response;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
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

    private void validateAdmin() {
        String role = RequestContext.getHeader("X-User-Role");
        if (!"ADMIN".equals(role))
            throw new UnauthorizedException("Solo el ADMIN puede realizar esta acción");
    }

    // ── Mapper ────────────────────────────────────────────────────────────────
    private LocationResponseDTO toLocationResponse(Location l) {
        LocationResponseDTO dto = new LocationResponseDTO();
        dto.setLocationId(l.getLocationId());
        dto.setName(l.getName());
        dto.setStoreId(l.getStoreId());
        return dto;
    }
}