package com.api.Transaction.repository;
import com.api.Transaction.entity.PlanChangeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface PlanChangeHistoryRepository extends JpaRepository<PlanChangeHistory, UUID> {
    List<PlanChangeHistory> findByStoreIdOrderByChangedAtDesc(UUID storeId);
}
