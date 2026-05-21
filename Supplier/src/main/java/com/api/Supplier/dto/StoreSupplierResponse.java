package com.api.Supplier.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StoreSupplierResponse {

        private UUID storeId;
        private SupplierResponse supplier;
        private OffsetDateTime createdAt;

}