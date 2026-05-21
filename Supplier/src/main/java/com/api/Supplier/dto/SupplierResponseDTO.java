package com.api.Supplier.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class SupplierResponseDTO {
    private UUID supplierId;
    private String name;
    private String contactName;
    private String phone;
    private String email;
    private OffsetDateTime createdAt;
}