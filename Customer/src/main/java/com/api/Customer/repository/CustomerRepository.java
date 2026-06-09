package com.api.Customer.repository;

import com.api.Customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    List<Customer> findByStoreIdOrderByCreatedAtDesc(UUID storeId);

    Optional<Customer> findByStoreIdAndCustomerId(UUID storeId, UUID customerId);

    Optional<Customer> findByStoreIdAndEmail(UUID storeId, String email);

    Optional<Customer> findByStoreIdAndPhone(UUID storeId, String phone);

    boolean existsByStoreIdAndEmail(UUID storeId, String email);

    long countByStoreId(UUID storeId);
}
