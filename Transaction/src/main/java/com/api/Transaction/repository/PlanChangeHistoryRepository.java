package com.api.Transaction.repository;
import com.api.Transaction.entity.PlanChangeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PlanChangeHistoryRepository extends JpaRepository<PlanChangeHistory, UUID> {
    List<PlanChangeHistory> findByStoreIdOrderByChangedAtDesc(UUID storeId);

    @Query("SELECT h FROM PlanChangeHistory h LEFT JOIN FETCH h.fromPlan LEFT JOIN FETCH h.toPlan")
    List<PlanChangeHistory> findAllWithPlans();
}
