package com.api.Preferences.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "wishlist")
public class Wishlist {

    @Id
    @GeneratedValue
    @Column(name = "wishlist_id")
    private UUID wishlistId;

    @Column(name = "user_id")
    private UUID userId;
}