package com.api.Store.repository;

import com.api.Store.entity.StoreCmsFaqConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoreCmsFaqConfigRepository extends JpaRepository<StoreCmsFaqConfig, UUID> {
}
