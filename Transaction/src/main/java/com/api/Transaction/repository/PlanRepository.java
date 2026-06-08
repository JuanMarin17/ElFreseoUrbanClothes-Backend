package com.api.Transaction.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.Transaction.entity.Plan;
import com.api.Transaction.enums.PlanName;

public interface PlanRepository extends JpaRepository<Plan, UUID> {
    Optional<Plan> findByName(PlanName name);
}