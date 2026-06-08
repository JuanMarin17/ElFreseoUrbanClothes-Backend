package com.api.Store.repository;

import com.api.Store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {
    Optional<Store> findBySlug(String slug);
    Optional<Store> findByName(String name);
    boolean existsBySlug(String slug);
    boolean existsByName(String name);
}
