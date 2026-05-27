package com.api.Reviews.entity;

import java.util.UUID;

import com.api.Reviews.enums.ReactionType;

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
@Table(name = "review_reaction")
public class ReviewReaction {

    @Id
    @GeneratedValue
    @Column(name = "reaction_id")
    private UUID reactionId;

    @Column(name = "review_id")
    private UUID reviewId;

    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type")
    private ReactionType reactionType;
}