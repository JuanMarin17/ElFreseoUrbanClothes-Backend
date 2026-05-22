package com.api.product.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.product.client.StoreClient;
import com.api.product.dto.CategoryRequestDTO;
import com.api.product.dto.CategoryResponseDTO;
import com.api.product.entity.Category;
import com.api.product.exception.BadRequestException;
import com.api.product.exception.ConflictException;
import com.api.product.exception.UnauthorizedException;
import com.api.product.repository.CategoryRepository;
import com.common_request_context_starter.context.RequestContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final StoreClient storeClient;

    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO dto) {
        validateAdminOrOwner();
        UUID storeId = getStoreIdFromHeader();

        if (dto == null || dto.getName() == null || dto.getName().isBlank())
            throw new BadRequestException("El nombre de la categoría es obligatorio");

        if (categoryRepository.existsByNameIgnoreCaseAndStoreId(dto.getName(), storeId))
            throw new ConflictException("Ya existe una categoría con ese nombre en esta tienda");

        Category category = Category.builder()
                .name(dto.getName())
                .storeId(storeId)
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();

        return mapToResponse(categoryRepository.save(category));
    }

    @Transactional
    public Optional<CategoryResponseDTO> updateCategory(UUID id, CategoryRequestDTO dto) {
        validateAdminOrOwner();
        UUID storeId = getStoreIdFromHeader();

        Optional<Category> optional = categoryRepository.findById(id);
        if (optional.isEmpty())
            return Optional.empty();

        Category category = optional.get();

        if (!category.getStoreId().equals(storeId))
            throw new UnauthorizedException("Esta categoría no pertenece a tu tienda");

        if (dto == null || dto.getName() == null || dto.getName().isBlank())
            throw new BadRequestException("El nombre de la categoría es obligatorio");

        if (!category.getName().equalsIgnoreCase(dto.getName()) &&
                categoryRepository.existsByNameIgnoreCaseAndStoreId(dto.getName(), storeId))
            throw new ConflictException("Ya existe otra categoría con ese nombre en esta tienda");

        category.setName(dto.getName());
        if (dto.getActive() != null)
            category.setActive(dto.getActive());

        return Optional.of(mapToResponse(categoryRepository.save(category)));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> listAllCategories() {
        UUID storeId = getStoreIdFromHeader();
        return categoryRepository.findByStoreIdAndActiveTrue(storeId)
                .stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> listAllActiveCategories() {
        UUID storeId = getStoreIdFromHeader();
        return categoryRepository.findByStoreIdAndActiveTrue(storeId)
                .stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public Optional<CategoryResponseDTO> getById(UUID id) {
        return categoryRepository.findById(id).map(this::mapToResponse);
    }

    @Transactional
    public Optional<CategoryResponseDTO> inactiveCategory(UUID id) {
        validateAdminOrOwner();
        UUID storeId = getStoreIdFromHeader();

        Optional<Category> optional = categoryRepository.findById(id);
        if (optional.isEmpty())
            return Optional.empty();

        Category category = optional.get();
        if (!category.getStoreId().equals(storeId))
            throw new UnauthorizedException("Esta categoría no pertenece a tu tienda");

        category.setActive(false);
        return Optional.of(mapToResponse(categoryRepository.save(category)));
    }

    @Transactional
    public Optional<CategoryResponseDTO> activeCategory(UUID id) {
        validateAdminOrOwner();
        UUID storeId = getStoreIdFromHeader();

        Optional<Category> optional = categoryRepository.findById(id);
        if (optional.isEmpty())
            return Optional.empty();

        Category category = optional.get();
        if (!category.getStoreId().equals(storeId))
            throw new UnauthorizedException("Esta categoría no pertenece a tu tienda");

        category.setActive(true);
        return Optional.of(mapToResponse(categoryRepository.save(category)));
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

    private CategoryResponseDTO mapToResponse(Category category) {
        return CategoryResponseDTO.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .status(Boolean.TRUE.equals(category.getActive()) ? "ACTIVE" : "INACTIVE")
                .build();
    }
}