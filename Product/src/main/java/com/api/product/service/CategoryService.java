package com.api.product.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.product.dto.CategoryRequestDTO;
import com.api.product.dto.CategoryResponseDTO;
import com.api.product.entity.Category;
import com.api.product.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

/**
 * Servicio para gestionar categorías (Category).
 * Incluye métodos para crear, actualizar, listar, buscar, activar y desactivar categorías.
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Crea una nueva categoría en la base de datos.
     * Valida campos obligatorios y verifica que no exista otra categoría con el mismo nombre.
     *
     * @param dto DTO con los datos de la categoría a crear
     * @return CategoryResponseDTO con los datos de la categoría creada
     */
    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO dto) {

        try {
            validateCategoryRequest(dto);

            // Validar si existe categoría con el mismo nombre
            if (categoryRepository.existsByNameIgnoreCase(dto.getName())) {
                throw new RuntimeException("La categoría ya existe");
            }

            // Crear entidad Category
            Category category = Category.builder()
                    .name(dto.getName())
                    .active(dto.getActive() != null ? dto.getActive() : true)
                    .build();

            // Guardar categoría
            Category saved = categoryRepository.save(category);

            return mapToResponse(saved);

        } catch (RuntimeException e) {
            throw new RuntimeException("Error al crear categoría: " + e.getMessage());
        }
    }

    /**
     * Actualiza una categoría existente.
     * Valida campos obligatorios y verifica que no se repita el nombre con otra categoría.
     *
     * @param id  UUID de la categoría
     * @param dto DTO con los datos actualizados
     * @return Optional con CategoryResponseDTO actualizado si existe
     */
    @Transactional
    public Optional<CategoryResponseDTO> updateCategory(UUID id, CategoryRequestDTO dto) {

        Optional<Category> optional = categoryRepository.findById(id);
        if (optional.isEmpty())
            return Optional.empty();

        Category category = optional.get();

        try {
            validateCategoryRequest(dto);

            // Validar nombre repetido
            if (!category.getName().equalsIgnoreCase(dto.getName())
                    && categoryRepository.existsByNameIgnoreCase(dto.getName())) {
                throw new RuntimeException("Ya existe otra categoría con el mismo nombre");
            }

            category.setName(dto.getName());

            // Actualizar active si viene en el DTO
            if (dto.getActive() != null) {
                category.setActive(dto.getActive());
            }

            Category saved = categoryRepository.save(category);

            return Optional.of(mapToResponse(saved));

        } catch (RuntimeException e) {
            throw new RuntimeException("Error al actualizar categoría: " + e.getMessage());
        }
    }

    /**
     * Lista todas las categorías registradas en el sistema.
     *
     * @return lista completa de categorías
     */
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> listAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lista todas las categorías activas en el sistema.
     *
     * @return lista completa de categorías activas
     */
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> listAllActiveCategories() {
        return categoryRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca una categoría por su ID.
     *
     * @param id UUID de la categoría
     * @return Optional con la categoría encontrada si existe
     */
    @Transactional(readOnly = true)
    public Optional<CategoryResponseDTO> getById(UUID id) {
        return categoryRepository.findById(id)
                .map(this::mapToResponse);
    }

    /**
     * Desactiva una categoría del sistema (Soft Delete).
     * Cambia el campo active a false.
     *
     * @param id UUID de la categoría
     * @return Optional con la categoría desactivada si existe
     */
    @Transactional
    public Optional<CategoryResponseDTO> inactiveCategory(UUID id) {

        Optional<Category> optional = categoryRepository.findById(id);
        if (optional.isEmpty())
            return Optional.empty();

        Category category = optional.get();
        category.setActive(false);

        categoryRepository.save(category);

        return Optional.of(mapToResponse(category));
    }

    /**
     * Activa una categoría del sistema.
     * Cambia el campo active a true.
     *
     * @param id UUID de la categoría
     * @return Optional con la categoría activada si existe
     */
    @Transactional
    public Optional<CategoryResponseDTO> activeCategory(UUID id) {

        Optional<Category> optional = categoryRepository.findById(id);
        if (optional.isEmpty())
            return Optional.empty();

        Category category = optional.get();
        category.setActive(true);

        categoryRepository.save(category);

        return Optional.of(mapToResponse(category));
    }

    /**
     * Valida campos obligatorios del CategoryRequestDTO.
     *
     * @param dto DTO a validar
     */
    private void validateCategoryRequest(CategoryRequestDTO dto) {

        if (dto == null || dto.getName() == null || dto.getName().isBlank()) {
            throw new RuntimeException("El nombre de la categoría es obligatorio");
        }
    }

    /**
     * Convierte entidad Category a CategoryResponseDTO.
     *
     * @param category entidad Category
     * @return CategoryResponseDTO
     */
    private CategoryResponseDTO mapToResponse(Category category) {
        return CategoryResponseDTO.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .status(category.getActive()? "ACTIVE":"INACTIVE")
                .build();
    }
}