package com.api.Store.service;

import com.api.Store.client.UserClient;
import com.api.Store.client.dto.UserInfoDTO;
import com.api.Store.dto.StoreUserRequestDTO;
import com.api.Store.dto.StoreUserResponseDTO;
import com.api.Store.entity.StoreUser;
import com.api.Store.enums.StoreRole;
import com.api.Store.exception.InvalidRoleException;
import com.api.Store.exception.StoreNotFoundException;
import com.api.Store.exception.UserAlreadyInStoreException;
import com.api.Store.repository.StoreRepository;
import com.api.Store.repository.StoreUserRepository;
import com.api.Store.util.HeaderUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreUserService {

    private final StoreRepository storeRepository;
    private final StoreUserRepository storeUserRepository;
    private final HeaderUtil headerUtil;
    private final UserClient userClient;

    // ── 1. Agregar usuario a tienda ──────────────────────────────────────────
    @Transactional
    public StoreUserResponseDTO addUserToStore(StoreUserRequestDTO dto) {

        UUID userId;

        if (dto.getUserId() == null) {
            userId = headerUtil.getUserIdFromHeader()
                    .orElseThrow(() -> new RuntimeException("Usuario no autenticado"));
            dto.setUserId(userId);
        }

        userId = dto.getUserId();

        if (!userClient.existUser(userId)) {
            throw new RuntimeException("Usuario no encontrado");
        }

        UUID storeId;

        if (dto.getStoreId() == null) {
            storeId = headerUtil.getStoreIdFromHeader()
                    .orElseThrow(() -> new RuntimeException("El id de la tienda es obligatorio"));
        }

        storeId = dto.getStoreId();

        dto.setStoreId(storeId);

        if (!storeRepository.existsById(dto.getStoreId()))
            throw new StoreNotFoundException("Tienda no encontrada con id: " + dto.getStoreId());

        if (storeUserRepository.existsByIdUserIdAndIdStoreId(dto.getUserId(), dto.getStoreId()))
            throw new UserAlreadyInStoreException("El usuario ya pertenece a esta tienda");

        // OWNER solo se asigna automáticamente al crear la tienda
        if (dto.getRole() == StoreRole.OWNER)
            throw new InvalidRoleException(
                    "No se puede asignar el rol OWNER manualmente. Se asigna al crear la tienda.");

        StoreUser storeUser = StoreUser.builder()
                .id(new StoreUser.StoreUserId(dto.getStoreId(), dto.getUserId()))
                .role(dto.getRole())
                .build();

        storeUserRepository.save(storeUser);

        return toResponse(storeUser);
    }

    // ── 2. Usuarios de una tienda (enriquecido con nombre y email) ──────────
    public List<StoreUserResponseDTO> getUsersByStore(UUID storeId) {
        if (!storeRepository.existsById(storeId))
            throw new StoreNotFoundException("Tienda no encontrada con id: " + storeId);

        return storeUserRepository.findByIdStoreId(storeId)
                .stream()
                .map(su -> {
                    var info = userClient.getUserById(su.getId().getUserId());
                    return toResponse(su, info.orElse(null));
                })
                .toList();
    }

    // ── 5. Activar/desactivar usuario en tienda ──────────────────────────────
    @Transactional
    public StoreUserResponseDTO toggleUserStatus(UUID storeId, UUID userId) {
        StoreUser storeUser = storeUserRepository
                .findByIdUserIdAndIdStoreId(userId, storeId)
                .orElseThrow(() -> new RuntimeException("El usuario no pertenece a esta tienda"));

        storeUser.setActive(!storeUser.isActive());
        storeUserRepository.save(storeUser);

        var info = userClient.getUserById(userId);
        return toResponse(storeUser, info.orElse(null));
    }

    // ── 3. Tiendas de un usuario ─────────────────────────────────────────────
    public List<StoreUserResponseDTO> getStoresByUser(UUID userId) {
        return storeUserRepository.findByIdUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── 4. Validar acceso de un usuario a una tienda ─────────────────────────
    public boolean validateAccess(UUID userId, UUID storeId) {
        return storeUserRepository.existsByIdUserIdAndIdStoreId(userId, storeId);
    }

    public StoreRole getUserRole(UUID userId, UUID storeId) {
        return storeUserRepository.findByIdUserIdAndIdStoreId(userId, storeId)
                .map(StoreUser::getRole)
                .orElseThrow(() -> new RuntimeException("El usuario no pertenece a esta tienda"));
    }

    // ── Mapper ───────────────────────────────────────────────────────────────
    private StoreUserResponseDTO toResponse(StoreUser u) {
        return toResponse(u, null);
    }

    private StoreUserResponseDTO toResponse(StoreUser u, UserInfoDTO info) {
        return StoreUserResponseDTO.builder()
                .userId(u.getId().getUserId())
                .storeId(u.getId().getStoreId())
                .role(u.getRole())
                .isActive(u.isActive())
                .userName(info != null ? info.getUserName() : null)
                .userEmail(info != null ? info.getUserEmail() : null)
                .build();
    }
}
