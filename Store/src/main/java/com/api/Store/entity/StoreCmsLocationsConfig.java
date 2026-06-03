package com.api.Store.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "cms_locations_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreCmsLocationsConfig {

    @Id
    @Column(name = "store_id")
    private UUID storeId;

    @Column(name = "show_map")
    private Boolean showMap;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
