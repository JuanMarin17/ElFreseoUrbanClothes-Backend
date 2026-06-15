package com.api.PosSale.dto;

import com.api.PosSale.enums.PosPaymentMethod;
import com.api.PosSale.enums.PosSaleStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PosSaleResponseDTO {

    private UUID saleId;
    private UUID storeId;
    private UUID employeeId;
    private UUID customerId;
    private String saleNumber;
    private PosSaleStatus status;
    private List<PosSaleItemResponseDTO> items;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal total;
    private PosPaymentMethod paymentMethod;
    private BigDecimal amountReceived;
    private BigDecimal change;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime cancelledAt;
}
