package com.api.Cms.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "cms_page")
public class CmsPage {

    @Id
    @GeneratedValue
    @Column(name = "page_id")
    private UUID pageId;

    @Column(name = "store_id")
    private UUID storeId;

    @Column(name = "user_id")
    private UUID userId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}