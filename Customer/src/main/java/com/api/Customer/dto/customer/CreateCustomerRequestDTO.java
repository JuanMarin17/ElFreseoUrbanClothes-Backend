package com.api.Customer.dto.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCustomerRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 150)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 50)
    private String document;

    @Size(max = 500)
    private String notes;
}
