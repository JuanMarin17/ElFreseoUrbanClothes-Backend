package com.tuapp.payments.repository;

import com.tuapp.payments.entity.CommissionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CommissionRecordRepository extends JpaRepository<CommissionRecord, UUID> {

    List<CommissionRecord> findByTenantId(String tenantId);

    @Query("SELECT COALESCE(SUM(c.commissionAmount), 0) FROM CommissionRecord c " +
           "WHERE c.tenantId = :tenantId AND c.createdAt BETWEEN :from AND :to")
    BigDecimal sumCommissionByTenantAndPeriod(String tenantId, LocalDateTime from, LocalDateTime to);

    @Query("SELECT COALESCE(SUM(c.commissionAmount), 0) FROM CommissionRecord c " +
           "WHERE c.createdAt BETWEEN :from AND :to")
    BigDecimal sumTotalCommissionByPeriod(LocalDateTime from, LocalDateTime to);
}
