package com.api.Store.repository;

import com.api.Store.entity.StoreUser;
import com.api.Store.entity.StoreUser.StoreUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface StoreUserRepository extends JpaRepository<StoreUser, StoreUserId> {
    List<StoreUser> findByIdUserId(UUID userId);

    List<StoreUser> findByIdStoreId(UUID storeId);

    boolean existsByIdUserIdAndIdStoreId(UUID userId, UUID storeId);
}
