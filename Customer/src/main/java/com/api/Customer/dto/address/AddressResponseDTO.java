package com.api.Customer.dto.address;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AddressResponseDTO {

    private UUID addressId;
    private String alias;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private Boolean isDefault;
    private LocalDateTime createdAt;
}
