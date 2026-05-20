package com.api.Supplier.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponse {

    private UUID supplierId;
    private String name;
    private String contactName;
    private String phone;
    private String email;
    private OffsetDateTime createdAt;

}