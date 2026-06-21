package com.api.Inventory.dto;

import lombok.Data;

@Data
public class LocationRequestDTO {
    private String name;
    private String address;
    private String description;
}