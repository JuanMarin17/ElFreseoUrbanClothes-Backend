package com.api.Store.repository;

import com.api.Store.entity.StoreSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface StoreSettingsRepository extends JpaRepository<StoreSettings, UUID> {
}
