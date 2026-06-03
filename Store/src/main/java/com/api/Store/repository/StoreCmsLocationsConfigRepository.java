package com.api.Store.repository;

import com.api.Store.entity.StoreCmsLocationsConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoreCmsLocationsConfigRepository extends JpaRepository<StoreCmsLocationsConfig, UUID> {
}
