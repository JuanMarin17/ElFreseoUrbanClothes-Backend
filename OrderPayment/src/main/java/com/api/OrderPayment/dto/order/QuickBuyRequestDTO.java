package com.api.OrderPayment.dto.order;

import com.api.OrderPayment.enums.PaymentMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class QuickBuyRequestDTO {

    @NotNull(message = "El productId es obligatorio")
    private UUID productId;

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String productName;

    private String variantName;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer quantity;

    @NotNull(message = "El precio unitario es obligatorio")
    private BigDecimal unitPrice;

    private ShippingAddressDTO shippingAddress;

    private PaymentMethod paymentMethod;

    private BigDecimal shippingCost;

    @Size(max = 500, message = "Las notas no pueden superar 500 caracteres")
    private String notes;

    private String couponCode;
}
