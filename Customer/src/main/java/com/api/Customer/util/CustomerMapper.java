package com.api.Customer.util;

import com.api.Customer.dto.address.AddressResponseDTO;
import com.api.Customer.dto.customer.CustomerResponseDTO;
import com.api.Customer.entity.Customer;
import com.api.Customer.entity.CustomerAddress;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomerMapper {

    public CustomerResponseDTO toDTO(Customer customer) {
        return CustomerResponseDTO.builder()
                .customerId(customer.getCustomerId())
                .storeId(customer.getStoreId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .document(customer.getDocument())
                .notes(customer.getNotes())
                .addresses(toAddressDTOs(customer.getAddresses()))
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    public AddressResponseDTO toAddressDTO(CustomerAddress address) {
        return AddressResponseDTO.builder()
                .addressId(address.getAddressId())
                .alias(address.getAlias())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .postalCode(address.getPostalCode())
                .isDefault(address.getIsDefault())
                .createdAt(address.getCreatedAt())
                .build();
    }

    public List<AddressResponseDTO> toAddressDTOs(List<CustomerAddress> addresses) {
        if (addresses == null) return List.of();
        return addresses.stream().map(this::toAddressDTO).toList();
    }
}
