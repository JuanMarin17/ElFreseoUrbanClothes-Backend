package com.api.Customer.dto.customer;

import com.api.Customer.dto.address.AddressResponseDTO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CustomerResponseDTO {

    private UUID customerId;
    private UUID storeId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String document;
    private String notes;
    private List<AddressResponseDTO> addresses;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
