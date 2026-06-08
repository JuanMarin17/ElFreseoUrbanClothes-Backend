package com.api.payments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.payments.entity.TenantMpCredential;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantMpCredentialRepository extends JpaRepository<TenantMpCredential, UUID> {

    Optional<TenantMpCredential> findByTenantId(String tenantId);

    Optional<TenantMpCredential> findByMpUserId(String mpUserId);
}
