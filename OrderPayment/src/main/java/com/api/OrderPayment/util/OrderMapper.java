package com.api.OrderPayment.util;

import com.api.OrderPayment.client.dto.UserInfoDTO;
import com.api.OrderPayment.dto.order.OrderItemResponseDTO;
import com.api.OrderPayment.dto.order.OrderResponseDTO;
import com.api.OrderPayment.dto.payment.PaymentResponseDTO;
import com.api.OrderPayment.entity.Order;
import com.api.OrderPayment.entity.OrderItem;
import com.api.OrderPayment.entity.Payment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponseDTO toDTO(Order order) {
        return toDTO(order, null);
    }

    public OrderResponseDTO toDTO(Order order, UserInfoDTO userInfo) {
        return OrderResponseDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .customerName(userInfo != null ? userInfo.getUserName() : null)
                .customerEmail(userInfo != null ? userInfo.getUserEmail() : null)
                .storeId(order.getStoreId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .items(toItemDTOs(order.getItems()))
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .discount(order.getDiscount())
                .total(order.getTotal())
                .shippingAddress(order.getShippingAddress())
                .notes(order.getNotes())
                .payment(order.getPayment() != null ? toPaymentDTO(order.getPayment()) : null)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public List<OrderItemResponseDTO> toItemDTOs(List<OrderItem> items) {
        return items.stream().map(this::toItemDTO).toList();
    }

    public OrderItemResponseDTO toItemDTO(OrderItem item) {
        return OrderItemResponseDTO.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .variantName(item.getVariantName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }

    public PaymentResponseDTO toPaymentDTO(Payment payment) {
        return PaymentResponseDTO.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .method(payment.getMethod())
                .transactionReference(payment.getTransactionReference())
                .details(payment.getDetails())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
