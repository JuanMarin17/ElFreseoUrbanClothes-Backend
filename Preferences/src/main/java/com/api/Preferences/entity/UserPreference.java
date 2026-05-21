package com.api.Preferences.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "user_preference")
public class UserPreference {

    @Id
    @GeneratedValue
    @Column(name = "preference_id")
    private UUID preferenceId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "preference_type")
    private String preferenceType;

    @Column(name = "preference_value")
    private String preferenceValue;
}