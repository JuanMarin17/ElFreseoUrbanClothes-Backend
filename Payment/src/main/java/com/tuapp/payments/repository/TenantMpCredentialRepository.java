package com.tuapp.payments.repository;

import com.tuapp.payments.entity.TenantMpCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantMpCredentialRepository extends JpaRepository<TenantMpCredential, UUID> {

    Optional<TenantMpCredential> findByTenantId(String tenantId);

    Optional<TenantMpCredential> findByMpUserId(String mpUserId);
}
