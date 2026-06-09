package com.api.PosSale.dto;

import com.api.PosSale.enums.PosPaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class CreatePosSaleRequestDTO {

    /** Cliente asociado a la venta (opcional: puede ser venta anónima) */
    private UUID customerId;

    @NotNull(message = "Los ítems son obligatorios")
    @NotEmpty(message = "Debe haber al menos un ítem en la venta")
    @Valid
    private List<PosSaleItemRequestDTO> items;

    @NotNull(message = "El método de pago es obligatorio")
    private PosPaymentMethod paymentMethod;

    /** Monto recibido del cliente (requerido para calcular el cambio en efectivo) */
    private BigDecimal amountReceived;

    /** Descuento global sobre el total de la venta */
    private BigDecimal discount;

    private String notes;
}
