package com.api.OrderPayment.dto.payment;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RefundRequestDTO {

    @Size(max = 500, message = "El motivo no puede superar 500 caracteres")
    private String reason;
}
