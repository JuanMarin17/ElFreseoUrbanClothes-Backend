package com.api.OrderPayment.dto.order;

import com.api.OrderPayment.enums.PaymentMethod;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateOrderRequestDTO {

    private ShippingAddressDTO shippingAddress;

    private PaymentMethod paymentMethod;

    private BigDecimal shippingCost;

    @Size(max = 500, message = "Las notas no pueden superar 500 caracteres")
    private String notes;

    private String couponCode;
}
