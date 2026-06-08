package com.api.Preferences.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.Preferences.entity.UserPreference;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, UUID> {
    List<UserPreference> findByUserId(UUID userId);
}