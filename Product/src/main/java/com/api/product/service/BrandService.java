package com.api.product.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.product.client.StoreClient;
import com.api.product.dto.BrandRequestDTO;
import com.api.product.dto.BrandResponseDTO;
import com.api.product.entity.Brand;
import com.api.product.exception.BadRequestException;
import com.api.product.exception.ConflictException;
import com.api.product.exception.ResourceNotFoundException;
import com.api.product.exception.UnauthorizedException;
import com.api.product.repository.BrandRepository;
import com.common_request_context_starter.context.RequestContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;
    private final StoreClient storeClient;

    @Transactional
    public BrandResponseDTO createBrand(BrandRequestDTO dto) {
        validateAdminOrOwner();
        UUID storeId = getStoreIdFromHeader();

        if (!storeClient.existsStore(storeId))
            throw new ResourceNotFoundException("La tienda no existe con id: " + storeId);

        if (dto == null || dto.getName() == null || dto.getName().isBlank())
            throw new BadRequestException("El nombre de la marca es obligatorio");

        if (brandRepository.existsByNameIgnoreCaseAndStoreId(dto.getName(), storeId))
            throw new ConflictException("Ya existe una marca con ese nombre en esta tienda");

        Brand brand = Brand.builder()
                .name(dto.getName())
                .storeId(storeId)
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();

        return mapToResponse(brandRepository.save(brand));
    }

    @Transactional
    public Optional<BrandResponseDTO> updateBrand(UUID id, BrandRequestDTO dto) {
        validateAdminOrOwner();
        UUID storeId = getStoreIdFromHeader();

        Optional<Brand> optional = brandRepository.findById(id);
        if (optional.isEmpty())
            return Optional.empty();

        Brand brand = optional.get();

        if (!brand.getStoreId().equals(storeId))
            throw new UnauthorizedException("Esta marca no pertenece a tu tienda");

        if (dto == null || dto.getName() == null || dto.getName().isBlank())
            throw new BadRequestException("El nombre de la marca es obligatorio");

        if (!brand.getName().equalsIgnoreCase(dto.getName()) &&
                brandRepository.existsByNameIgnoreCaseAndStoreId(dto.getName(), storeId))
            throw new ConflictException("Ya existe otra marca con ese nombre en esta tienda");

        brand.setName(dto.getName());
        if (dto.getActive() != null)
            brand.setActive(dto.getActive());

        return Optional.of(mapToResponse(brandRepository.save(brand)));
    }

    @Transactional(readOnly = true)
    public List<BrandResponseDTO> listAllBrands() {
        UUID storeId = getStoreIdFromHeader();
        return brandRepository.findByStoreIdAndActiveTrue(storeId)
                .stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<BrandResponseDTO> listAllActiveBrands() {
        UUID storeId = getStoreIdFromHeader();
        return brandRepository.findByStoreIdAndActiveTrue(storeId)
                .stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public Optional<BrandResponseDTO> getById(UUID id) {
        return brandRepository.findById(id).map(this::mapToResponse);
    }

    @Transactional
    public Optional<BrandResponseDTO> inactiveBrand(UUID id) {
        validateAdminOrOwner();
        UUID storeId = getStoreIdFromHeader();

        Optional<Brand> optional = brandRepository.findById(id);
        if (optional.isEmpty())
            return Optional.empty();

        Brand brand = optional.get();
        if (!brand.getStoreId().equals(storeId))
            throw new UnauthorizedException("Esta marca no pertenece a tu tienda");

        brand.setActive(false);
        return Optional.of(mapToResponse(brandRepository.save(brand)));
    }

    @Transactional
    public Optional<BrandResponseDTO> activeBrand(UUID id) {
        validateAdminOrOwner();
        UUID storeId = getStoreIdFromHeader();

        Optional<Brand> optional = brandRepository.findById(id);
        if (optional.isEmpty())
            return Optional.empty();

        Brand brand = optional.get();
        if (!brand.getStoreId().equals(storeId))
            throw new UnauthorizedException("Esta marca no pertenece a tu tienda");

        brand.setActive(true);
        return Optional.of(mapToResponse(brandRepository.save(brand)));
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

    private void validateAdminOrOwner() {
        String role = RequestContext.getHeader("X-User-Role");
        if (!"ADMIN".equals(role) && !"OWNER".equals(role))
            throw new UnauthorizedException("Solo el ADMIN u OWNER pueden realizar esta acción");
    }

    private BrandResponseDTO mapToResponse(Brand brand) {
        return BrandResponseDTO.builder()
                .brandId(brand.getBrandId())
                .name(brand.getName())
                .status(Boolean.TRUE.equals(brand.getActive()) ? "ACTIVE" : "INACTIVE")
                .build();
    }
}