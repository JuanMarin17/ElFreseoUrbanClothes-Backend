package com.api.Store.entity;

import com.api.Store.enums.StoreRole;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "store_user")
public class StoreUser {

    @EmbeddedId
    private StoreUserId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoreRole role;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreUserId implements Serializable {
        @Column(name = "store_id")
        private UUID storeId;

        @Column(name = "user_id")
        private UUID userId;
    }
}
