package com.api.product.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.product.dto.BrandRequestDTO;
import com.api.product.dto.BrandResponseDTO;
import com.api.product.entity.Brand;
import com.api.product.repository.BrandRepository;

import lombok.RequiredArgsConstructor;

/**
 * Servicio para gestionar marcas (Brand).
 * Incluye métodos para crear, actualizar, listar, buscar, activar y desactivar marcas.
 */
@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    /**
     * Crea una nueva marca en la base de datos.
     * Valida campos obligatorios y verifica que no exista otra marca con el mismo nombre.
     *
     * @param dto DTO con los datos de la marca a crear
     * @return BrandResponseDTO con los datos de la marca creada
     */
    @Transactional
    public BrandResponseDTO createBrand(BrandRequestDTO dto) {

        try {
            validateBrandRequest(dto);

            // Validar si existe marca con el mismo nombre
            if (brandRepository.existsByNameIgnoreCase(dto.getName())) {
                throw new RuntimeException("La marca ya existe");
            }

            // Crear entidad Brand
            Brand brand = Brand.builder()
                    .name(dto.getName())
                    .active(dto.getActive() != null ? dto.getActive() : true)
                    .build();

            // Guardar marca
            Brand saved = brandRepository.save(brand);

            return mapToResponse(saved);

        } catch (RuntimeException e) {
            throw new RuntimeException("Error al crear marca: " + e.getMessage());
        }
    }

    /**
     * Actualiza una marca existente.
     * Valida campos obligatorios y verifica que no se repita el nombre con otra marca.
     *
     * @param id  UUID de la marca
     * @param dto DTO con los datos actualizados
     * @return Optional con BrandResponseDTO actualizado si existe
     */
    @Transactional
    public Optional<BrandResponseDTO> updateBrand(UUID id, BrandRequestDTO dto) {

        Optional<Brand> optional = brandRepository.findById(id);
        if (optional.isEmpty())
            return Optional.empty();

        Brand brand = optional.get();

        try {
            validateBrandRequest(dto);

            // Validar nombre repetido
            if (!brand.getName().equalsIgnoreCase(dto.getName())
                    && brandRepository.existsByNameIgnoreCase(dto.getName())) {
                throw new RuntimeException("Ya existe otra marca con el mismo nombre");
            }

            brand.setName(dto.getName());

            // Actualizar active si viene en el DTO
            if (dto.getActive() != null) {
                brand.setActive(dto.getActive());
            }

            Brand saved = brandRepository.save(brand);

            return Optional.of(mapToResponse(saved));

        } catch (RuntimeException e) {
            throw new RuntimeException("Error al actualizar marca: " + e.getMessage());
        }
    }

    /**
     * Lista todas las marcas registradas en el sistema.
     *
     * @return lista completa de marcas
     */
    @Transactional(readOnly = true)
    public List<BrandResponseDTO> listAllBrands() {
        return brandRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lista todas las marcas activas en el sistema.
     *
     * @return lista completa de marcas activas
     */
    @Transactional(readOnly = true)
    public List<BrandResponseDTO> listAllActiveBrands() {
        return brandRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca una marca por su ID.
     *
     * @param id UUID de la marca
     * @return Optional con la marca encontrada si existe
     */
    @Transactional(readOnly = true)
    public Optional<BrandResponseDTO> getById(UUID id) {
        return brandRepository.findById(id)
                .map(this::mapToResponse);
    }

    /**
     * Desactiva una marca del sistema (Soft Delete).
     * Cambia el campo active a false.
     *
     * @param id UUID de la marca
     * @return Optional con la marca desactivada si existe
     */
    @Transactional
    public Optional<BrandResponseDTO> inactiveBrand(UUID id) {

        Optional<Brand> optional = brandRepository.findById(id);
        if (optional.isEmpty())
            return Optional.empty();

        Brand brand = optional.get();
        brand.setActive(false);

        brandRepository.save(brand);

        return Optional.of(mapToResponse(brand));
    }

    /**
     * Activa una marca del sistema.
     * Cambia el campo active a true.
     *
     * @param id UUID de la marca
     * @return Optional con la marca activada si existe
     */
    @Transactional
    public Optional<BrandResponseDTO> activeBrand(UUID id) {

        Optional<Brand> optional = brandRepository.findById(id);
        if (optional.isEmpty())
            return Optional.empty();

        Brand brand = optional.get();
        brand.setActive(true);

        brandRepository.save(brand);

        return Optional.of(mapToResponse(brand));
    }

    /**
     * Valida campos obligatorios del BrandRequestDTO.
     *
     * @param dto DTO a validar
     */
    private void validateBrandRequest(BrandRequestDTO dto) {

        if (dto == null || dto.getName() == null || dto.getName().isBlank()) {
            throw new RuntimeException("El nombre de la marca es obligatorio");
        }
    }

    /**
     * Convierte entidad Brand a BrandResponseDTO.
     *
     * @param brand entidad Brand
     * @return BrandResponseDTO
     */
    private BrandResponseDTO mapToResponse(Brand brand) {
        return BrandResponseDTO.builder()
                .brandId(brand.getBrandId())
                .name(brand.getName())
                .status(brand.getActive()? "ACTIVE":"INACTIVE")
                .build();
    }
}