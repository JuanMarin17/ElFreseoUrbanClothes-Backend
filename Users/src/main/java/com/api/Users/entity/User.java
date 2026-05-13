package com.api.Users.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    private UUID userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "image_profile")
    private String imageProfile;
}
