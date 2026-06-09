package com.api.Customer.repository;

import com.api.Customer.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, UUID> {

    List<CustomerAddress> findByCustomerCustomerId(UUID customerId);

    Optional<CustomerAddress> findByAddressIdAndCustomerCustomerId(UUID addressId, UUID customerId);

    @Modifying
    @Query("UPDATE CustomerAddress a SET a.isDefault = false WHERE a.customer.customerId = :customerId AND a.addressId <> :excludeId")
    void clearDefaultExcept(UUID customerId, UUID excludeId);
}
