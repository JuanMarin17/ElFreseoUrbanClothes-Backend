package com.api.OrderPayment.dto.order;

import com.api.OrderPayment.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusRequestDTO {

    @NotNull(message = "El estado es obligatorio")
    private OrderStatus status;
}
