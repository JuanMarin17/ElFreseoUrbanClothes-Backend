package com.api.Transaction.entity;

import java.math.BigDecimal;
import java.util.UUID;

import com.api.Transaction.enums.PlanName;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "plan")
public class Plan {

    @Id
    @GeneratedValue
    @Column(name = "plan_id")
    private UUID planId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private PlanName name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "max_products")
    private Integer maxProducts;

    @Column(name = "max_pages")
    private Integer maxPages;

    @Column(name = "max_ai_calls")
    private Integer maxAiCalls;

    // Otras limitaciones en formato JSON flexible
    @Column(columnDefinition = "jsonb")
    private String features;
}