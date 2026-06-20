package com.api.Customer.service;

import com.api.Customer.client.StoreClient;
import com.api.Customer.dto.address.AddressResponseDTO;
import com.api.Customer.dto.address.CreateAddressRequestDTO;
import com.api.Customer.dto.address.UpdateAddressRequestDTO;
import com.api.Customer.dto.customer.CreateCustomerRequestDTO;
import com.api.Customer.dto.customer.CustomerResponseDTO;
import com.api.Customer.dto.customer.UpdateCustomerRequestDTO;
import com.api.Customer.entity.Customer;
import com.api.Customer.entity.CustomerAddress;
import com.api.Customer.exception.CustomerNotFoundException;
import com.api.Customer.exception.UnauthorizedAccessException;
import com.api.Customer.repository.CustomerAddressRepository;
import com.api.Customer.repository.CustomerRepository;
import com.api.Customer.util.CustomerMapper;
import com.api.Customer.util.HeaderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository addressRepository;
    private final CustomerMapper mapper;
    private final HeaderUtil headerUtil;
    private final StoreClient storeClient;

    /** Verifica que el usuario autenticado pertenezca a la tienda antes de operar sobre sus clientes. */
    private void requireStoreAccess(UUID storeId) {
        UUID userId = headerUtil.requireUserId();
        if (!storeClient.hasAccess(storeId, userId)) {
            throw new UnauthorizedAccessException("No tienes acceso a esta tienda.");
        }
    }

    @Transactional
    public CustomerResponseDTO createCustomer(UUID storeId, CreateCustomerRequestDTO dto) {
        requireStoreAccess(storeId);
        if (dto.getEmail() != null && !dto.getEmail().isBlank()
                && customerRepository.existsByStoreIdAndEmail(storeId, dto.getEmail())) {
            throw new IllegalStateException("Ya existe un cliente con ese email en esta tienda.");
        }

        Customer customer = Customer.builder()
                .storeId(storeId)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .document(dto.getDocument())
                .notes(dto.getNotes())
                .build();

        Customer saved = customerRepository.save(customer);
        log.info("Cliente creado: {} en tienda {}", saved.getCustomerId(), storeId);
        return mapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponseDTO> getCustomersByStore(UUID storeId) {
        requireStoreAccess(storeId);
        return customerRepository.findByStoreIdOrderByCreatedAtDesc(storeId)
                .stream().map(mapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponseDTO getCustomer(UUID storeId, UUID customerId) {
        requireStoreAccess(storeId);
        return mapper.toDTO(findCustomer(storeId, customerId));
    }

    @Transactional(readOnly = true)
    public CustomerResponseDTO searchByEmail(UUID storeId, String email) {
        requireStoreAccess(storeId);
        Customer customer = customerRepository.findByStoreIdAndEmail(storeId, email)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "No se encontró cliente con email '" + email + "' en esta tienda."));
        return mapper.toDTO(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponseDTO searchByPhone(UUID storeId, String phone) {
        requireStoreAccess(storeId);
        Customer customer = customerRepository.findByStoreIdAndPhone(storeId, phone)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "No se encontró cliente con teléfono '" + phone + "' en esta tienda."));
        return mapper.toDTO(customer);
    }

    @Transactional
    public CustomerResponseDTO updateCustomer(UUID storeId, UUID customerId, UpdateCustomerRequestDTO dto) {
        requireStoreAccess(storeId);
        Customer customer = findCustomer(storeId, customerId);

        if (dto.getEmail() != null && !dto.getEmail().isBlank()
                && !dto.getEmail().equals(customer.getEmail())
                && customerRepository.existsByStoreIdAndEmail(storeId, dto.getEmail())) {
            throw new IllegalStateException("Ya existe un cliente con ese email en esta tienda.");
        }

        if (dto.getFirstName() != null) customer.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) customer.setLastName(dto.getLastName());
        if (dto.getEmail() != null) customer.setEmail(dto.getEmail());
        if (dto.getPhone() != null) customer.setPhone(dto.getPhone());
        if (dto.getDocument() != null) customer.setDocument(dto.getDocument());
        if (dto.getNotes() != null) customer.setNotes(dto.getNotes());

        return mapper.toDTO(customerRepository.save(customer));
    }

    @Transactional
    public void deleteCustomer(UUID storeId, UUID customerId) {
        requireStoreAccess(storeId);
        Customer customer = findCustomer(storeId, customerId);
        customerRepository.delete(customer);
        log.info("Cliente eliminado: {} de tienda {}", customerId, storeId);
    }

    // ── Direcciones ───────────────────────────────────────────────────────────

    @Transactional
    public AddressResponseDTO addAddress(UUID storeId, UUID customerId, CreateAddressRequestDTO dto) {
        requireStoreAccess(storeId);
        Customer customer = findCustomer(storeId, customerId);

        boolean setDefault = Boolean.TRUE.equals(dto.getIsDefault());

        CustomerAddress address = CustomerAddress.builder()
                .customer(customer)
                .alias(dto.getAlias())
                .street(dto.getStreet())
                .city(dto.getCity())
                .state(dto.getState())
                .country(dto.getCountry())
                .postalCode(dto.getPostalCode())
                .isDefault(setDefault)
                .build();

        CustomerAddress saved = addressRepository.save(address);

        if (setDefault) {
            addressRepository.clearDefaultExcept(customerId, saved.getAddressId());
        }

        return mapper.toAddressDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<AddressResponseDTO> getAddresses(UUID storeId, UUID customerId) {
        requireStoreAccess(storeId);
        findCustomer(storeId, customerId);
        return addressRepository.findByCustomerCustomerId(customerId)
                .stream().map(mapper::toAddressDTO).toList();
    }

    @Transactional
    public AddressResponseDTO updateAddress(UUID storeId, UUID customerId, UUID addressId,
                                            UpdateAddressRequestDTO dto) {
        requireStoreAccess(storeId);
        findCustomer(storeId, customerId);
        CustomerAddress address = addressRepository.findByAddressIdAndCustomerCustomerId(addressId, customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Dirección no encontrada: " + addressId));

        if (dto.getAlias() != null) address.setAlias(dto.getAlias());
        if (dto.getStreet() != null) address.setStreet(dto.getStreet());
        if (dto.getCity() != null) address.setCity(dto.getCity());
        if (dto.getState() != null) address.setState(dto.getState());
        if (dto.getCountry() != null) address.setCountry(dto.getCountry());
        if (dto.getPostalCode() != null) address.setPostalCode(dto.getPostalCode());

        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            address.setIsDefault(true);
            CustomerAddress saved = addressRepository.save(address);
            addressRepository.clearDefaultExcept(customerId, saved.getAddressId());
            return mapper.toAddressDTO(saved);
        }

        return mapper.toAddressDTO(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(UUID storeId, UUID customerId, UUID addressId) {
        requireStoreAccess(storeId);
        findCustomer(storeId, customerId);
        CustomerAddress address = addressRepository.findByAddressIdAndCustomerCustomerId(addressId, customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Dirección no encontrada: " + addressId));
        addressRepository.delete(address);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Customer findCustomer(UUID storeId, UUID customerId) {
        return customerRepository.findByStoreIdAndCustomerId(storeId, customerId)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Cliente no encontrado: " + customerId));
    }
}
