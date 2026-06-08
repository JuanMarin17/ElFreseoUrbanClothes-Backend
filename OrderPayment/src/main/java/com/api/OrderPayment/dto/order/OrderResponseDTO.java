package com.api.OrderPayment.dto.order;

import com.api.OrderPayment.dto.payment.PaymentResponseDTO;
import com.api.OrderPayment.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderResponseDTO {
    private UUID id;
    private UUID userId;
    private UUID storeId;
    private String orderNumber;
    private OrderStatus status;
    private List<OrderItemResponseDTO> items;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal total;
    private String shippingAddress;
    private String notes;
    private PaymentResponseDTO payment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
