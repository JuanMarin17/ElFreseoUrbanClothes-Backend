package com.api.Store.repository;

import com.api.Store.entity.StoreCmsReturns;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoreCmsReturnsRepository extends JpaRepository<StoreCmsReturns, UUID> {
}
