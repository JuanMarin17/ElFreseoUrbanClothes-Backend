package com.api.Store.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "cms_contact")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreCmsContact {

    @Id
    @Column(name = "store_id")
    private UUID storeId;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "whatsapp", length = 50)
    private String whatsapp;

    @Column(name = "instagram", length = 100)
    private String instagram;

    @Column(name = "tiktok", length = 100)
    private String tiktok;

    @Column(name = "hours", length = 255)
    private String hours;

    @Column(name = "form_title", length = 255)
    private String formTitle;

    @Column(name = "form_subtitle", length = 255)
    private String formSubtitle;

    @Column(name = "show_form")
    private Boolean showForm;

    @Column(name = "show_socials")
    private Boolean showSocials;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
