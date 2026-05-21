package com.api.Preferences.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.Preferences.entity.UserBehavior;

public interface UserBehaviorRepository extends JpaRepository<UserBehavior, UUID> {
    List<UserBehavior> findByUserId(UUID userId);
}