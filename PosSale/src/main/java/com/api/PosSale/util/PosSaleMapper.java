package com.api.PosSale.util;

import com.api.PosSale.dto.PosSaleItemResponseDTO;
import com.api.PosSale.dto.PosSaleResponseDTO;
import com.api.PosSale.entity.PosSale;
import com.api.PosSale.entity.PosSaleItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PosSaleMapper {

    public PosSaleResponseDTO toDTO(PosSale sale) {
        return PosSaleResponseDTO.builder()
                .saleId(sale.getSaleId())
                .storeId(sale.getStoreId())
                .employeeId(sale.getEmployeeId())
                .customerId(sale.getCustomerId())
                .saleNumber(sale.getSaleNumber())
                .status(sale.getStatus())
                .items(toItemDTOs(sale.getItems()))
                .subtotal(sale.getSubtotal())
                .discount(sale.getDiscount())
                .tax(sale.getTax())
                .total(sale.getTotal())
                .paymentMethod(sale.getPaymentMethod())
                .amountReceived(sale.getAmountReceived())
                .change(sale.getChange())
                .notes(sale.getNotes())
                .createdAt(sale.getCreatedAt())
                .updatedAt(sale.getUpdatedAt())
                .cancelledAt(sale.getCancelledAt())
                .build();
    }

    public List<PosSaleItemResponseDTO> toItemDTOs(List<PosSaleItem> items) {
        if (items == null) return List.of();
        return items.stream().map(this::toItemDTO).toList();
    }

    public PosSaleItemResponseDTO toItemDTO(PosSaleItem item) {
        return PosSaleItemResponseDTO.builder()
                .itemId(item.getItemId())
                .productId(item.getProductId())
                .variantId(item.getVariantId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discount(item.getDiscount())
                .subtotal(item.getSubtotal())
                .build();
    }
}
