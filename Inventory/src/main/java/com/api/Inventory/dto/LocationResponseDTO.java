package com.api.Inventory.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class LocationResponseDTO {
    private UUID locationId;
    private String name;
    private String address;
    private String description;
    private UUID storeId;
}