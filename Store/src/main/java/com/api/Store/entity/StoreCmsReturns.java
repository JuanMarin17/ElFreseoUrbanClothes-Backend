package com.api.Store.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "cms_returns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreCmsReturns {

    @Id
    @Column(name = "store_id")
    private UUID storeId;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "intro", columnDefinition = "TEXT")
    private String intro;

    @Column(name = "days")
    private Short days;

    @Column(name = "conditions", columnDefinition = "TEXT")
    private String conditions;

    @Column(name = "process", columnDefinition = "TEXT")
    private String process;

    @Column(name = "exceptions", columnDefinition = "TEXT")
    private String exceptions;

    @Column(name = "refund_method", length = 20)
    private String refundMethod;

    @Column(name = "allow_exchanges")
    private Boolean allowExchanges;

    @Column(name = "allow_refunds")
    private Boolean allowRefunds;

    @Column(name = "require_receipt")
    private Boolean requireReceipt;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
