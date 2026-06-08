package com.api.LoyalCustomer.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.LoyalCustomer.entity.LoyaltyAccount;

public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, UUID> {
    Optional<LoyaltyAccount> findByUserIdAndStoreId(UUID userId, UUID storeId);
}