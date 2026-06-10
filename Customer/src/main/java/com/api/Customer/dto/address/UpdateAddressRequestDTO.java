package com.api.Customer.dto.address;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateAddressRequestDTO {

    @Size(max = 50)
    private String alias;

    @Size(max = 250)
    private String street;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 100)
    private String country;

    @Size(max = 20)
    private String postalCode;

    private Boolean isDefault;
}
