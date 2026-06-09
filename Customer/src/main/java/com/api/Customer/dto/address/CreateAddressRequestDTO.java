package com.api.Customer.dto.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAddressRequestDTO {

    @Size(max = 50)
    private String alias;

    @NotBlank(message = "La calle es obligatoria")
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
