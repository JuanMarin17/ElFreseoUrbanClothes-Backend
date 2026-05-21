package com.api.Supplier.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.api.Supplier.dto.AssignSupplierRequest;
import com.api.Supplier.dto.CreateSupplierRequest;
import com.api.Supplier.dto.MessageResponseDTO;
import com.api.Supplier.dto.StoreSupplierResponse;
import com.api.Supplier.dto.SupplierResponse;
import com.api.Supplier.dto.UpdateSupplierRequest;
import com.api.Supplier.entity.StoreSupplier;
import com.api.Supplier.entity.Supplier;
import com.api.Supplier.exception.StoreSupplierAlreadyExistsException;
import com.api.Supplier.exception.StoreSupplierNotFoundException;
import com.api.Supplier.exception.SupplierNotFoundException;
import com.api.Supplier.repository.StoreSupplierRepository;
import com.api.Supplier.repository.SupplierRepository;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Data
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final StoreSupplierRepository storeSupplierRepository;

    /**
     * Crea un nuevo supplier.
     *
     * @param request datos del supplier a crear
     * @return mensaje de confirmación
     */
    public MessageResponseDTO createSupplier(CreateSupplierRequest request) {
        Supplier supplierEntity = new Supplier();
        supplierEntity.setName(request.getName());
        supplierEntity.setContactName(request.getContactName());
        supplierEntity.setPhone(request.getPhone());
        supplierEntity.setEmail(request.getEmail());

        Supplier created = supplierRepository.save(supplierEntity);

        if (created == null) {
            throw new RuntimeException("No se creó el supplier correctamente");
        }

        MessageResponseDTO response = new MessageResponseDTO();
        response.setMessage("Supplier creado correctamente");
        response.setStatus(201);

        return response;
    }

    /**
     * Obtiene todos los suppliers paginados.
     *
     * @param pageable configuración de paginación
     * @return página de suppliers
     */
    public Page<SupplierResponse> getAllSuppliers(Pageable pageable) {
        return supplierRepository.findAll(pageable)
                .map(this::toSupplierResponse);
    }

    /**
     * Obtiene un supplier por su ID.
     *
     * @param supplierId identificador único del supplier
     * @return datos del supplier encontrado
     */
    public SupplierResponse getSupplierById(UUID supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier no encontrado"));

        return toSupplierResponse(supplier);
    }

    /**
     * Actualiza la información de un supplier.
     * Solo actualiza los campos que vienen con valor.
     *
     * @param supplierId identificador del supplier a actualizar
     * @param request    datos a actualizar
     * @return mensaje de confirmación
     */
    public MessageResponseDTO updateSupplier(UUID supplierId, UpdateSupplierRequest request) {
        Supplier supplierEntity = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier no encontrado"));

        if (request.getName() != null) {
            supplierEntity.setName(request.getName());
        }

        if (request.getContactName() != null) {
            supplierEntity.setContactName(request.getContactName());
        }

        if (request.getPhone() != null) {
            supplierEntity.setPhone(request.getPhone());
        }

        if (request.getEmail() != null) {
            supplierEntity.setEmail(request.getEmail());
        }

        supplierRepository.save(supplierEntity);

        MessageResponseDTO response = new MessageResponseDTO();
        response.setMessage("Supplier actualizado correctamente");
        response.setStatus(200);

        return response;
    }

    /**
     * Elimina un supplier por su ID.
     *
     * @param supplierId identificador del supplier a eliminar
     * @return mensaje de confirmación
     */
    public MessageResponseDTO deleteSupplier(UUID supplierId) {
        Supplier supplierEntity = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier no encontrado"));

        supplierRepository.delete(supplierEntity);

        MessageResponseDTO response = new MessageResponseDTO();
        response.setMessage("Supplier eliminado correctamente");
        response.setStatus(200);

        return response;
    }

    /**
     * Obtiene todos los suppliers asignados a una tienda.
     *
     * @param storeId identificador de la tienda
     * @return lista de suppliers de la tienda
     */
    public List<StoreSupplierResponse> getSuppliersByStore(UUID storeId) {
        List<StoreSupplier> relations = storeSupplierRepository.findByStoreIdWithSupplier(storeId);

        return relations.stream()
                .map(this::toStoreSupplierResponse)
                .collect(Collectors.toList());
    }

    /**
     * Asigna un supplier existente a una tienda.
     *
     * @param storeId identificador de la tienda
     * @param request contiene el supplierId a asignar
     * @return mensaje de confirmación
     */
    public MessageResponseDTO assignSupplierToStore(UUID storeId, AssignSupplierRequest request) {
        UUID supplierId = request.getSupplierId();

        if (storeSupplierRepository.existsByStoreIdAndSupplierId(storeId, supplierId)) {
            throw new StoreSupplierAlreadyExistsException(storeId, supplierId);
        }

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier no encontrado"));

        StoreSupplier storeSupplier = new StoreSupplier();
        storeSupplier.setStoreId(storeId);
        storeSupplier.setSupplierId(supplierId);
        storeSupplier.setSupplier(supplier);

        storeSupplierRepository.save(storeSupplier);

        MessageResponseDTO response = new MessageResponseDTO();
        response.setMessage("Supplier asignado a la tienda correctamente");
        response.setStatus(201);

        return response;
    }

    /**
     * Elimina la relación entre una tienda y un supplier.
     *
     * @param storeId    identificador de la tienda
     * @param supplierId identificador del supplier
     * @return mensaje de confirmación
     */
    public MessageResponseDTO removeSupplierFromStore(UUID storeId, UUID supplierId) {
        if (!storeSupplierRepository.existsByStoreIdAndSupplierId(storeId, supplierId)) {
            throw new StoreSupplierNotFoundException(storeId, supplierId);
        }

        storeSupplierRepository.deleteByStoreIdAndSupplierId(storeId, supplierId);

        MessageResponseDTO response = new MessageResponseDTO();
        response.setMessage("Supplier removido de la tienda correctamente");
        response.setStatus(200);

        return response;
    }

    // ── Mapeo interno ─────────────────────────────────────────────────────────

    private SupplierResponse toSupplierResponse(Supplier supplier) {
        SupplierResponse response = new SupplierResponse();
        response.setSupplierId(supplier.getSupplierId());
        response.setName(supplier.getName());
        response.setContactName(supplier.getContactName());
        response.setPhone(supplier.getPhone());
        response.setEmail(supplier.getEmail());
        response.setCreatedAt(supplier.getCreatedAt());
        return response;
    }

    private StoreSupplierResponse toStoreSupplierResponse(StoreSupplier ss) {
        StoreSupplierResponse response = new StoreSupplierResponse();
        response.setStoreId(ss.getStoreId());
        response.setSupplier(toSupplierResponse(ss.getSupplier()));
        response.setCreatedAt(ss.getCreatedAt());
        return response;
    }
}