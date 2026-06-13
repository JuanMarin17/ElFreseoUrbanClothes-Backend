package com.api.Supplier.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.Supplier.client.ProductClient;
import com.api.Supplier.dto.MessageResponseDTO;
import com.api.Supplier.dto.ProductSummaryDTO;
import com.api.Supplier.dto.SupplierRequestDTO;
import com.api.Supplier.dto.SupplierResponseDTO;
import com.api.Supplier.entity.StoreSupplier;
import com.api.Supplier.entity.Supplier;
import com.api.Supplier.entity.SupplierProduct;
import com.api.Supplier.exception.BadRequestException;
import com.api.Supplier.exception.SupplierAlreadyExistsException;
import com.api.Supplier.exception.SupplierNotFoundException;
import com.api.Supplier.exception.SupplierProductAlreadyLinkedException;
import com.api.Supplier.exception.UnauthorizedUserException;
import com.api.Supplier.repository.StoreSupplierRepository;
import com.api.Supplier.repository.SupplierProductRepository;
import com.api.Supplier.repository.SupplierRepository;
import com.common_request_context_starter.context.RequestContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final StoreSupplierRepository storeSupplierRepository;
    private final SupplierProductRepository supplierProductRepository;
    private final ProductClient productClient;

    // ── Crear proveedor y vincularlo a la tienda ─────────────────────────────
    @Transactional
    public SupplierResponseDTO createSupplier(SupplierRequestDTO dto) {
        UUID storeId = getStoreIdFromHeader();

        if (supplierRepository.existsByName(dto.getName()))
            throw new SupplierAlreadyExistsException("Ya existe un proveedor con el nombre: " + dto.getName());

        Supplier supplier = new Supplier();
        supplier.setName(dto.getName());
        supplier.setContactName(dto.getContactName());
        supplier.setPhone(dto.getPhone());
        supplier.setEmail(dto.getEmail());

        Supplier saved = supplierRepository.save(supplier);

        // Vincular proveedor a la tienda
        StoreSupplier storeSupplier = new StoreSupplier();
        StoreSupplier.StoreSuppliedId id = new StoreSupplier.StoreSuppliedId();
        id.setStoreId(storeId);
        id.setSupplierId(saved.getSupplierId());
        storeSupplier.setId(id);
        storeSupplierRepository.save(storeSupplier);

        return toResponse(saved);
    }

    // ── Obtener proveedores de la tienda ─────────────────────────────────────
    public List<SupplierResponseDTO> getSuppliersByStore() {
        UUID storeId = getStoreIdFromHeader();

        return storeSupplierRepository.findByIdStoreId(storeId)
                .stream()
                .map(ss -> supplierRepository.findBySupplierIdAndIsActiveTrue(ss.getId().getSupplierId())
                        .orElse(null))
                .filter(s -> s != null) // excluir inactivos
                .map(this::toResponse)
                .toList();
    }

    public SupplierResponseDTO getSupplierById(UUID supplierId) {
        Supplier supplier = supplierRepository.findBySupplierIdAndIsActiveTrue(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException("Proveedor no encontrado con id: " + supplierId));
        return toResponse(supplier);
    }

    // ── Actualizar proveedor ──────────────────────────────────────────────────
    @Transactional
    public SupplierResponseDTO updateSupplier(UUID supplierId, SupplierRequestDTO dto) {
        UUID storeId = getStoreIdFromHeader();

        if (!storeSupplierRepository.existsByIdStoreIdAndIdSupplierId(storeId, supplierId))
            throw new BadRequestException("El proveedor no pertenece a esta tienda");

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException("Proveedor no encontrado con id: " + supplierId));

        if (dto.getName() != null)
            supplier.setName(dto.getName());
        if (dto.getContactName() != null)
            supplier.setContactName(dto.getContactName());
        if (dto.getPhone() != null)
            supplier.setPhone(dto.getPhone());
        if (dto.getEmail() != null)
            supplier.setEmail(dto.getEmail());

        return toResponse(supplierRepository.save(supplier));
    }

    // Solo elimina la relación store_supplier
    @Transactional
    public MessageResponseDTO unlinkSupplierFromStore(UUID supplierId) {
        UUID storeId = getStoreIdFromHeader();

        StoreSupplier.StoreSuppliedId id = new StoreSupplier.StoreSuppliedId();
        id.setStoreId(storeId);
        id.setSupplierId(supplierId);

        if (!storeSupplierRepository.existsById(id))
            throw new BadRequestException("El proveedor no pertenece a esta tienda");

        storeSupplierRepository.deleteById(id);

        MessageResponseDTO response = new MessageResponseDTO();
        response.setMessage("Proveedor desvinculado de la tienda correctamente");
        response.setStatus(200);
        return response;
    }

    // Eliminado lógico del proveedor
    @Transactional
    public MessageResponseDTO deactivateSupplier(UUID supplierId) {
        String role = RequestContext.getHeader("X-User-Role");

        if (role == null || !role.equals("ADMIN"))
            throw new UnauthorizedUserException("No tienes permisos para desactivar un proveedor");

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException("Proveedor no encontrado con id: " + supplierId));

        supplier.setIsActive(false);
        supplierRepository.save(supplier);

        MessageResponseDTO response = new MessageResponseDTO();
        response.setMessage("Proveedor desactivado correctamente");
        response.setStatus(200);
        return response;
    }

    // ── Vincular producto a proveedor ─────────────────────────────────────────
    @Transactional
    public MessageResponseDTO linkProductToSupplier(UUID supplierId, UUID productId) {
        UUID storeId = getStoreIdFromHeader();

        if (!storeSupplierRepository.existsByIdStoreIdAndIdSupplierId(storeId, supplierId))
            throw new BadRequestException("El proveedor no pertenece a esta tienda");

        if (!productClient.productExistsInStore(productId, storeId))
            throw new BadRequestException("El producto no existe en esta tienda");

        SupplierProduct.SupplierProductId id = new SupplierProduct.SupplierProductId();
        id.setSupplierId(supplierId);
        id.setProductId(productId);

        if (supplierProductRepository.existsById(id))
            throw new SupplierProductAlreadyLinkedException("El producto ya está vinculado a este proveedor");

        SupplierProduct link = new SupplierProduct();
        link.setId(id);
        link.setStoreId(storeId);
        supplierProductRepository.save(link);

        MessageResponseDTO response = new MessageResponseDTO();
        response.setMessage("Producto vinculado al proveedor correctamente");
        response.setStatus(200);
        return response;
    }

    // ── Desvincular producto de proveedor ─────────────────────────────────────
    @Transactional
    public MessageResponseDTO unlinkProductFromSupplier(UUID supplierId, UUID productId) {
        UUID storeId = getStoreIdFromHeader();

        if (!storeSupplierRepository.existsByIdStoreIdAndIdSupplierId(storeId, supplierId))
            throw new BadRequestException("El proveedor no pertenece a esta tienda");

        SupplierProduct.SupplierProductId id = new SupplierProduct.SupplierProductId();
        id.setSupplierId(supplierId);
        id.setProductId(productId);

        if (!supplierProductRepository.existsById(id))
            throw new BadRequestException("El producto no está vinculado a este proveedor");

        supplierProductRepository.deleteById(id);

        MessageResponseDTO response = new MessageResponseDTO();
        response.setMessage("Producto desvinculado del proveedor correctamente");
        response.setStatus(200);
        return response;
    }

    // ── Obtener productos vinculados a un proveedor ───────────────────────────
    public List<ProductSummaryDTO> getProductsBySupplier(UUID supplierId) {
        UUID storeId = getStoreIdFromHeader();

        if (!storeSupplierRepository.existsByIdStoreIdAndIdSupplierId(storeId, supplierId))
            throw new BadRequestException("El proveedor no pertenece a esta tienda");

        return supplierProductRepository.findByIdSupplierIdAndStoreId(supplierId, storeId)
                .stream()
                .map(link -> productClient.getProductById(link.getId().getProductId(), storeId).orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    // ── Helper: obtener storeId del header ───────────────────────────────────
    private UUID getStoreIdFromHeader() {
        String storeIdHeader = RequestContext.getHeader("X-Store-Id");
        if (storeIdHeader == null || storeIdHeader.isBlank())
            throw new BadRequestException("No se encontró el X-Store-Id en el header");
        try {
            return UUID.fromString(storeIdHeader);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Formato inválido del storeId");
        }
    }

    // ── Mapper ───────────────────────────────────────────────────────────────
    private SupplierResponseDTO toResponse(Supplier s) {
        SupplierResponseDTO dto = new SupplierResponseDTO();
        dto.setSupplierId(s.getSupplierId());
        dto.setName(s.getName());
        dto.setContactName(s.getContactName());
        dto.setPhone(s.getPhone());
        dto.setEmail(s.getEmail());
        dto.setCreatedAt(s.getCreatedAt());
        return dto;
    }
}