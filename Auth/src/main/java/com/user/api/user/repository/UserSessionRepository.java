package com.user.api.user.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.user.api.user.entity.UserSession;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
    List<UserSession> findByUserIdAndActiveTrue(UUID userId);

    @Modifying
    @Transactional
    @Query("UPDATE UserSession s SET s.active = false WHERE s.userId = :userId AND s.active = true")
    void deactivateAllByUserId(UUID userId);
}
