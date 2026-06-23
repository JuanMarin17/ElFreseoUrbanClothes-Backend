package com.api.Customer.controller;

import com.api.Customer.dto.address.AddressResponseDTO;
import com.api.Customer.dto.address.CreateAddressRequestDTO;
import com.api.Customer.dto.address.UpdateAddressRequestDTO;
import com.api.Customer.dto.customer.CreateCustomerRequestDTO;
import com.api.Customer.dto.customer.CustomerResponseDTO;
import com.api.Customer.dto.customer.UpdateCustomerRequestDTO;
import com.api.Customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * POST   /stores/{storeId}/customers                                       → Crear cliente
 * GET    /stores/{storeId}/customers                                        → Listar clientes de la tienda
 * GET    /stores/{storeId}/customers/{customerId}                           → Ver cliente
 * PUT    /stores/{storeId}/customers/{customerId}                           → Actualizar cliente
 * DELETE /stores/{storeId}/customers/{customerId}                           → Eliminar cliente
 * GET    /stores/{storeId}/customers/search?email=&phone=                   → Buscar por contacto
 *
 * POST   /stores/{storeId}/customers/{customerId}/addresses                 → Agregar dirección
 * GET    /stores/{storeId}/customers/{customerId}/addresses                 → Listar direcciones
 * PUT    /stores/{storeId}/customers/{customerId}/addresses/{addressId}     → Actualizar dirección
 * DELETE /stores/{storeId}/customers/{customerId}/addresses/{addressId}     → Eliminar dirección
 */
@RestController
@RequestMapping("/stores/{storeId}/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponseDTO> createCustomer(
            @PathVariable UUID storeId,
            @Valid @RequestBody CreateCustomerRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(customerService.createCustomer(storeId, dto));
    }

    @GetMapping
    public ResponseEntity<Page<CustomerResponseDTO>> listCustomers(
            @PathVariable UUID storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(customerService.getCustomersByStore(storeId, pageable));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponseDTO> getCustomer(
            @PathVariable UUID storeId,
            @PathVariable UUID customerId) {
        return ResponseEntity.ok(customerService.getCustomer(storeId, customerId));
    }

    @GetMapping("/search")
    public ResponseEntity<CustomerResponseDTO> search(
            @PathVariable UUID storeId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone) {

        if (email != null && !email.isBlank()) {
            return ResponseEntity.ok(customerService.searchByEmail(storeId, email));
        }
        if (phone != null && !phone.isBlank()) {
            return ResponseEntity.ok(customerService.searchByPhone(storeId, phone));
        }
        throw new IllegalArgumentException("Debes proporcionar 'email' o 'phone' para buscar.");
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(
            @PathVariable UUID storeId,
            @PathVariable UUID customerId,
            @Valid @RequestBody UpdateCustomerRequestDTO dto) {
        return ResponseEntity.ok(customerService.updateCustomer(storeId, customerId, dto));
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomer(
            @PathVariable UUID storeId,
            @PathVariable UUID customerId) {
        customerService.deleteCustomer(storeId, customerId);
        return ResponseEntity.noContent().build();
    }

    // ── Direcciones ───────────────────────────────────────────────────────────

    @PostMapping("/{customerId}/addresses")
    public ResponseEntity<AddressResponseDTO> addAddress(
            @PathVariable UUID storeId,
            @PathVariable UUID customerId,
            @Valid @RequestBody CreateAddressRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(customerService.addAddress(storeId, customerId, dto));
    }

    @GetMapping("/{customerId}/addresses")
    public ResponseEntity<List<AddressResponseDTO>> getAddresses(
            @PathVariable UUID storeId,
            @PathVariable UUID customerId) {
        return ResponseEntity.ok(customerService.getAddresses(storeId, customerId));
    }

    @PutMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<AddressResponseDTO> updateAddress(
            @PathVariable UUID storeId,
            @PathVariable UUID customerId,
            @PathVariable UUID addressId,
            @Valid @RequestBody UpdateAddressRequestDTO dto) {
        return ResponseEntity.ok(customerService.updateAddress(storeId, customerId, addressId, dto));
    }

    @DeleteMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable UUID storeId,
            @PathVariable UUID customerId,
            @PathVariable UUID addressId) {
        customerService.deleteAddress(storeId, customerId, addressId);
        return ResponseEntity.noContent().build();
    }
}
