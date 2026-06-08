package com.api.Store.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "cms_faq_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreCmsFaqConfig {

    @Id
    @Column(name = "store_id")
    private UUID storeId;

    @Column(name = "page_title", length = 255)
    private String pageTitle;

    @Column(name = "page_subtitle", length = 255)
    private String pageSubtitle;

    @Column(name = "show_search")
    private Boolean showSearch;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
