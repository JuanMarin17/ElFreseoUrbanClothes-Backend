package com.api.Store.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "cms_about")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreCmsAbout {

    @Id
    @Column(name = "store_id")
    private UUID storeId;

    @Column(name = "headline", length = 255)
    private String headline;

    @Column(name = "story", columnDefinition = "TEXT")
    private String story;

    @Column(name = "mission", columnDefinition = "TEXT")
    private String mission;

    @Column(name = "vision", columnDefinition = "TEXT")
    private String vision;

    @Column(name = "founded", length = 10)
    private String founded;

    @Column(name = "team_size", length = 50)
    private String teamSize;

    @Column(name = "show_team")
    private Boolean showTeam;

    @Column(name = "show_timeline")
    private Boolean showTimeline;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
