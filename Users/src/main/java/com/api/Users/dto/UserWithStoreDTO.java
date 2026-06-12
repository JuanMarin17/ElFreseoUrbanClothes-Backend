package com.api.Users.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWithStoreDTO {

    private UUID userId;
    private String userName;
    private String phone;
    private String imageProfile;
    private boolean hasStore;
    private StoreDTO store;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreDTO {
        private UUID storeId;
        private String name;
        private String slug;
        private Boolean isActive;
    }
}
