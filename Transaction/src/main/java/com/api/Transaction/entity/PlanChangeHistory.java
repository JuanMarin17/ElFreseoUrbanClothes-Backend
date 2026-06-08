package com.api.Transaction.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.api.Transaction.enums.PlanChangeReason;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "plan_change_history")
public class PlanChangeHistory {

    @Id
    @GeneratedValue
    @Column(name = "history_id")
    private UUID historyId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_plan_id")
    private Plan fromPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_plan_id", nullable = false)
    private Plan toPlan;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    @Enumerated(EnumType.STRING)
    private PlanChangeReason reason;
}