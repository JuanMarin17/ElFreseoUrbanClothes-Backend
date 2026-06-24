package com.api.Store.service;

import com.api.Store.dto.StoreCreateRequestDTO;
import com.api.Store.dto.StoreResponseDTO;
import com.api.Store.dto.StoreToggleStatusRequestDTO;
import com.api.Store.entity.Store;
import com.api.Store.entity.StoreUser;
import com.api.Store.enums.StoreRole;
import com.api.Store.exception.StoreAlreadyExistsException;
import com.api.Store.exception.StoreNotFoundException;
import com.api.Store.exception.UnauthorizedStoreActionException;
import com.api.Store.client.NotificationClient;
import com.api.Store.entity.StoreSettings;
import com.api.Store.repository.StoreRepository;
import com.api.Store.repository.StoreSettingsRepository;
import com.api.Store.repository.StoreUserRepository;
import com.api.Store.util.HeaderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreUserRepository storeUserRepository;
    private final StoreSettingsRepository storeSettingsRepository;
    private final StoreCmsService cmsService;
    private final HeaderUtil headerUtil;
    private final NotificationClient notificationClient;

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

        cmsService.saveCms(saved.getStoreId(), dto.getCms());

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

    // ── 3. Inhabilitar / habilitar tienda (solo SUPERADMIN) ──────────────────
    @Transactional
    public StoreResponseDTO toggleStatus(UUID storeId, StoreToggleStatusRequestDTO dto) {
        String role = headerUtil.getHeader("X-User-Role").orElse(null);
        if (!"SUPERADMIN".equals(role))
            throw new UnauthorizedStoreActionException("Solo SUPERADMIN puede inhabilitar o habilitar tiendas");

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Tienda no encontrada con id: " + storeId));

        store.setIsActive(dto.getIsActive());
        store.setDisabledReason(Boolean.TRUE.equals(dto.getIsActive()) ? null : dto.getReason());

        Store saved = storeRepository.save(store);

        notificationClient.sendStoreStatusChanged(
                saved.getOwnerId(), saved.getStoreId(), saved.getName(),
                Boolean.TRUE.equals(dto.getIsActive()), saved.getDisabledReason());

        String message = Boolean.TRUE.equals(dto.getIsActive())
                ? "Tienda habilitada correctamente"
                : "Tienda inhabilitada correctamente";

        return toResponse(saved, message, 200);
    }

    // ── Mapper ───────────────────────────────────────────────────────────────
    private StoreResponseDTO toResponse(Store store, String message, int status) {
        return StoreResponseDTO.builder()
                .storeId(store.getStoreId())
                .ownerId(store.getOwnerId())
                .name(store.getName())
                .slug(store.getSlug())
                .description(store.getDescription())
                .isActive(store.getIsActive())
                .disabledReason(store.getDisabledReason())
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

    /**
     * Obtener todas las tiendas. Acotado a 1000 para evitar cargar la plataforma entera en memoria.
     *
     * @param excludeMaintenance si es true, excluye las tiendas con maintenance.enabled=true
     *                           (para listados públicos, ej. el marketplace). Por defecto false,
     *                           para no afectar a los consumidores internos (panel SUPERADMIN, Users).
     */
    public List<StoreResponseDTO> getAllStores(boolean excludeMaintenance) {
        List<Store> stores = storeRepository.findAll(PageRequest.of(0, 1000)).getContent();

        if (excludeMaintenance) {
            Set<UUID> storeIds = stores.stream().map(Store::getStoreId).collect(Collectors.toSet());
            Map<UUID, StoreSettings> settingsByStore = storeSettingsRepository.findAllById(storeIds)
                    .stream()
                    .collect(Collectors.toMap(StoreSettings::getStoreId, s -> s));

            stores = stores.stream()
                    .filter(store -> !isInMaintenance(settingsByStore.get(store.getStoreId())))
                    .toList();
        }

        return stores.stream()
                .map(store -> toResponse(store, "Tienda encontrada", 200))
                .collect(Collectors.toList());
    }

    private boolean isInMaintenance(StoreSettings settings) {
        if (settings == null || settings.getMaintenance() == null) return false;
        return Boolean.TRUE.equals(settings.getMaintenance().get("enabled"));
    }
}

