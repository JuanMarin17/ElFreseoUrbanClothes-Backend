package com.api.Store.service;

import com.api.Store.dto.StoreCreateRequestDTO;
import com.api.Store.dto.StoreResponseDTO;
import com.api.Store.dto.StoreUserRequestDTO;
import com.api.Store.dto.StoreUserResponseDTO;
import com.api.Store.dto.settings.StoreSettingsRequestDTO;
import com.api.Store.dto.settings.StoreSettingsResponseDTO;
import com.api.Store.entity.Store;
import com.api.Store.entity.StoreSettings;
import com.api.Store.entity.StoreUser;
import com.api.Store.enums.StoreRole;
import com.api.Store.exception.InvalidRoleException;
import com.api.Store.exception.StoreAlreadyExistsException;
import com.api.Store.exception.StoreNotFoundException;
import com.api.Store.exception.UserAlreadyInStoreException;
import com.api.Store.repository.StoreRepository;
import com.api.Store.repository.StoreSettingsRepository;
import com.api.Store.repository.StoreUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreUserRepository storeUserRepository;
    private final StoreSettingsRepository storeSettingsRepository;

    // ── 1. Crear tienda ─────────────────────────────────────────────────────
    @Transactional
    public StoreResponseDTO createStore(StoreCreateRequestDTO dto) {
        if (storeRepository.existsBySlug(dto.getSlug())) {
            throw new StoreAlreadyExistsException("Ya existe un negocio con la url: " + dto.getSlug());
        }
        if (storeRepository.existsByName(dto.getName())) {
            throw new StoreAlreadyExistsException("Ya existe un negocio con el nombre: " + dto.getName());
        }

        Store store = Store.builder()
                .ownerId(dto.getOwnerId())
                .name(dto.getName())
                .slug(dto.getSlug())
                .description(dto.getDescription())
                .isActive(true)
                .build();

        Store saved = storeRepository.save(store);

        // Al crear la tienda, el dueño se registra automáticamente como OWNER en store_user
        StoreUser ownerEntry = StoreUser.builder()
                .id(new StoreUser.StoreUserId(saved.getStoreId(), dto.getOwnerId()))
                .role(StoreRole.OWNER)
                .build();
        storeUserRepository.save(ownerEntry);

        return toStoreResponse(saved, "Negocio creado correctamente", 201);
    }

    // ── 2. Obtener tienda por ID ─────────────────────────────────────────────
    public StoreResponseDTO getStoreById(UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Tienda no encontrada con id: " + storeId));
        return toStoreResponse(store, "Tienda encontrada", 200);
    }

    // ── 3. Agregar usuario a tienda ──────────────────────────────────────────
    @Transactional
    public StoreUserResponseDTO addUserToStore(StoreUserRequestDTO dto) {
        storeRepository.findById(dto.getStoreId())
                .orElseThrow(() -> new StoreNotFoundException("Tienda no encontrada con id: " + dto.getStoreId()));

        if (storeUserRepository.existsByIdUserIdAndIdStoreId(dto.getUserId(), dto.getStoreId())) {
            throw new UserAlreadyInStoreException("El usuario ya pertenece a esta tienda");
        }

        // Solo ADMIN y STAFF pueden agregarse (el OWNER se asigna al crear la tienda)
        if (dto.getRole() == StoreRole.OWNER) {
            throw new InvalidRoleException("No se puede asignar el rol OWNER manualmente. Este rol se asigna al crear la tienda.");
        }

        StoreUser storeUser = StoreUser.builder()
                .id(new StoreUser.StoreUserId(dto.getStoreId(), dto.getUserId()))
                .role(dto.getRole())
                .build();

        storeUserRepository.save(storeUser);

        return StoreUserResponseDTO.builder()
                .userId(dto.getUserId())
                .storeId(dto.getStoreId())
                .role(dto.getRole())
                .build();
    }

    // ── 4. Listar usuarios de una tienda ────────────────────────────────────
    public List<StoreUserResponseDTO> getUsersByStore(UUID storeId) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Tienda no encontrada con id: " + storeId));

        return storeUserRepository.findByIdStoreId(storeId).stream()
                .map(u -> StoreUserResponseDTO.builder()
                        .userId(u.getId().getUserId())
                        .storeId(u.getId().getStoreId())
                        .role(u.getRole())
                        .build())
                .toList();
    }

    // ── 5. Tiendas de un usuario ─────────────────────────────────────────────
    public List<StoreUserResponseDTO> getStoresByUser(UUID userId) {
        return storeUserRepository.findByIdUserId(userId).stream()
                .map(u -> StoreUserResponseDTO.builder()
                        .userId(u.getId().getUserId())
                        .storeId(u.getId().getStoreId())
                        .role(u.getRole())
                        .build())
                .toList();
    }

    // ── 6. Validar acceso de usuario a tienda ────────────────────────────────
    public boolean validateUserAccess(UUID userId, UUID storeId) {
        return storeUserRepository.existsByIdUserIdAndIdStoreId(userId, storeId);
    }

    // ── 7. Obtener settings ──────────────────────────────────────────────────
    public StoreSettingsResponseDTO getSettings(UUID storeId) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Tienda no encontrada con id: " + storeId));

        StoreSettings settings = storeSettingsRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("La tienda aún no tiene configuración"));

        return toSettingsResponse(settings);
    }

    // ── 8. Guardar / actualizar settings ─────────────────────────────────────
    @Transactional
    public StoreSettingsResponseDTO saveSettings(UUID storeId, StoreSettingsRequestDTO dto) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Tienda no encontrada con id: " + storeId));

        StoreSettings settings = storeSettingsRepository.findById(storeId)
                .orElse(StoreSettings.builder().storeId(storeId).build());

        if (dto.getLogoUrl() != null)       settings.setLogoUrl(dto.getLogoUrl());
        if (dto.getBannerUrl() != null)     settings.setBannerUrl(dto.getBannerUrl());
        if (dto.getPrimaryColor() != null)  settings.setPrimaryColor(dto.getPrimaryColor());
        if (dto.getSecondaryColor() != null)settings.setSecondaryColor(dto.getSecondaryColor());
        if (dto.getFont() != null)          settings.setFont(dto.getFont());
        if (dto.getTheme() != null)         settings.setTheme(dto.getTheme());
        if (dto.getLayout() != null)        settings.setLayout(dto.getLayout());
        if (dto.getCurrency() != null)      settings.setCurrency(dto.getCurrency());
        if (dto.getLanguage() != null)      settings.setLanguage(dto.getLanguage());
        settings.setUpdatedAt(OffsetDateTime.now());

        return toSettingsResponse(storeSettingsRepository.save(settings));
    }

    // ── Mappers ──────────────────────────────────────────────────────────────
    private StoreResponseDTO toStoreResponse(Store store, String message, int status) {
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

    private StoreSettingsResponseDTO toSettingsResponse(StoreSettings s) {
        return StoreSettingsResponseDTO.builder()
                .storeId(s.getStoreId())
                .logoUrl(s.getLogoUrl())
                .bannerUrl(s.getBannerUrl())
                .primaryColor(s.getPrimaryColor())
                .secondaryColor(s.getSecondaryColor())
                .font(s.getFont())
                .theme(s.getTheme())
                .layout(s.getLayout())
                .currency(s.getCurrency())
                .language(s.getLanguage())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
