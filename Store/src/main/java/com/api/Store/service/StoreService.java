package com.api.Store.service;

import com.api.Store.dto.StoreCreateRequestDTO;
import com.api.Store.dto.StoreResponseDTO;
import com.api.Store.entity.Store;
import com.api.Store.entity.StoreUser;
import com.api.Store.enums.StoreRole;
import com.api.Store.exception.StoreAlreadyExistsException;
import com.api.Store.exception.StoreNotFoundException;
import com.api.Store.repository.StoreRepository;
import com.api.Store.repository.StoreUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreUserRepository storeUserRepository;

    // ── 1. Crear tienda ──────────────────────────────────────────────────────
    @Transactional
    public StoreResponseDTO createStore(StoreCreateRequestDTO dto) {
        if (storeRepository.existsBySlug(dto.getSlug()))
            throw new StoreAlreadyExistsException("Ya existe una tienda con la url: " + dto.getSlug());

        if (storeRepository.existsByName(dto.getName()))
            throw new StoreAlreadyExistsException("Ya existe una tienda con el nombre: " + dto.getName());

        Store store = Store.builder()
                .ownerId(dto.getOwnerId())
                .name(dto.getName())
                .slug(dto.getSlug())
                .description(dto.getDescription())
                .isActive(true)
                .build();

        Store saved = storeRepository.save(store);

        // El dueño se registra automáticamente como OWNER
        StoreUser ownerEntry = StoreUser.builder()
                .id(new StoreUser.StoreUserId(saved.getStoreId(), dto.getOwnerId()))
                .role(StoreRole.OWNER)
                .build();
        storeUserRepository.save(ownerEntry);

        return toResponse(saved, "Tienda creada correctamente", 201);
    }

    // ── 2. Obtener tienda por ID ─────────────────────────────────────────────
    public StoreResponseDTO getStoreById(UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Tienda no encontrada con id: " + storeId));
        return toResponse(store, "Tienda encontrada", 200);
    }

    public StoreResponseDTO getBySlug(String slug){
        Store store = storeRepository.findBySlug(slug)
                .orElseThrow(() -> new StoreNotFoundException("Tienda no encontrada con el slug: " + slug));
        return toResponse(store, "Tienda encontrada", 200);
    }

    // ── Mapper ───────────────────────────────────────────────────────────────
    private StoreResponseDTO toResponse(Store store, String message, int status) {
        return StoreResponseDTO.builder()
                .storeId(store.getStoreId())
                .name(store.getName())
                .slug(store.getSlug())
                .description(store.getDescription())
                .isActive(store.getIsActive())
                .message(message)
                .status(status)
                .build();
    }

    public Boolean existStore(UUID storeId) {
        Optional<Store> store = storeRepository.findById(storeId);

        if (store.isEmpty()) {
            return false;
        }

        return true;
    }

    /** Obtener todas las tiendas */
    public List<StoreResponseDTO> getAllStores() {
        List<Store> stores = storeRepository.findAll();
        return stores.stream()
                .map(store -> toResponse(store, "Tienda encontrada", 200))
                .collect(Collectors.toList());
    }
}

