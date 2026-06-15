package com.api.OrderPayment.dto.order;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddressDTO {
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String department;
}
