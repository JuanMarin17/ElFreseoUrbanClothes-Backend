package com.api.PosSale.service;

import com.api.PosSale.client.InventoryClient;
import com.api.PosSale.client.StoreClient;
import com.api.PosSale.dto.CreatePosSaleRequestDTO;
import com.api.PosSale.dto.DailySummaryResponseDTO;
import com.api.PosSale.dto.PosSaleResponseDTO;
import com.api.PosSale.entity.PosSale;
import com.api.PosSale.entity.PosSaleItem;
import com.api.PosSale.enums.PosPaymentMethod;
import com.api.PosSale.enums.PosSaleStatus;
import com.api.PosSale.exception.ForbiddenException;
import com.api.PosSale.exception.PosSaleNotFoundException;
import com.api.PosSale.repository.PosSaleRepository;
import com.api.PosSale.util.PosSaleMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PosSaleService {

    private static final Set<String> ALLOWED_ROLES = Set.of("ADMIN", "OWNER", "STAFF");

    private final PosSaleRepository saleRepository;
    private final InventoryClient inventoryClient;
    private final StoreClient storeClient;
    private final PosSaleMapper mapper;
    private final HttpServletRequest httpRequest;

    @Transactional
    public PosSaleResponseDTO createSale(UUID storeId, UUID employeeId, CreatePosSaleRequestDTO dto) {
        validateRole(storeId);

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío");
        }

        BigDecimal subtotal = dto.getItems().stream()
                .map(item -> {
                    BigDecimal itemDiscount = item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO;
                    return item.getUnitPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()))
                            .subtract(itemDiscount);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal globalDiscount = dto.getDiscount() != null ? dto.getDiscount() : BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(globalDiscount).max(BigDecimal.ZERO);

        BigDecimal amountReceived;
        BigDecimal change;

        if (dto.getPaymentMethod() == PosPaymentMethod.CASH) {
            amountReceived = dto.getAmountReceived() != null ? dto.getAmountReceived() : BigDecimal.ZERO;
            if (amountReceived.compareTo(total) < 0) {
                throw new IllegalArgumentException("Monto recibido insuficiente");
            }
            change = amountReceived.subtract(total).max(BigDecimal.ZERO);
        } else {
            amountReceived = total;
            change = BigDecimal.ZERO;
        }

        PosSale sale = PosSale.builder()
                .storeId(storeId)
                .employeeId(employeeId)
                .customerId(dto.getCustomerId())
                .saleNumber(generateSaleNumber(storeId))
                .status(PosSaleStatus.COMPLETED)
                .subtotal(subtotal)
                .discount(globalDiscount)
                .tax(BigDecimal.ZERO)
                .total(total)
                .paymentMethod(dto.getPaymentMethod())
                .amountReceived(amountReceived)
                .change(change)
                .notes(dto.getNotes())
                .build();

        List<PosSaleItem> items = dto.getItems().stream()
                .map(req -> {
                    BigDecimal itemDiscount = req.getDiscount() != null ? req.getDiscount() : BigDecimal.ZERO;
                    BigDecimal itemSubtotal = req.getUnitPrice()
                            .multiply(BigDecimal.valueOf(req.getQuantity()))
                            .subtract(itemDiscount);

                    return PosSaleItem.builder()
                            .sale(sale)
                            .productId(req.getProductId())
                            .variantId(req.getVariantId())
                            .productName(req.getProductName())
                            .quantity(req.getQuantity())
                            .unitPrice(req.getUnitPrice())
                            .discount(itemDiscount)
                            .subtotal(itemSubtotal)
                            .build();
                })
                .toList();

        sale.getItems().addAll(items);
        PosSale saved = saleRepository.save(sale);
        log.info("Venta POS creada: {}", saved.getSaleNumber());

        List<Map<String, Object>> movements = items.stream()
                .filter(item -> item.getVariantId() != null)
                .map(item -> InventoryClient.movement(item.getVariantId(), item.getQuantity(), "OUT"))
                .toList();
        inventoryClient.registerMovementsBatch(storeId, movements);

        return mapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<PosSaleResponseDTO> getSalesByStore(UUID storeId, PosSaleStatus status, int page, int size) {
        validateRole(storeId);
        Pageable pageable = PageRequest.of(page, size);
        if (status != null) {
            return saleRepository.findByStoreIdAndStatusOrderByCreatedAtDesc(storeId, status, pageable)
                    .stream().map(mapper::toDTO).toList();
        }
        return saleRepository.findByStoreIdOrderByCreatedAtDesc(storeId, pageable)
                .stream().map(mapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public PosSaleResponseDTO getSale(UUID storeId, UUID saleId) {
        validateRole(storeId);
        return mapper.toDTO(findSale(storeId, saleId));
    }

    @Transactional(readOnly = true)
    public List<PosSaleResponseDTO> getSalesByCustomer(UUID storeId, UUID customerId, int page, int size) {
        validateRole(storeId);
        Pageable pageable = PageRequest.of(page, size);
        return saleRepository.findByStoreIdAndCustomerIdOrderByCreatedAtDesc(storeId, customerId, pageable)
                .stream().map(mapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<PosSaleResponseDTO> getSalesByDateRange(UUID storeId, LocalDateTime from, LocalDateTime to, int page, int size) {
        validateRole(storeId);
        Pageable pageable = PageRequest.of(page, size);
        return saleRepository.findByStoreIdAndCreatedAtBetweenOrderByCreatedAtDesc(storeId, from, to, pageable)
                .stream().map(mapper::toDTO).toList();
    }

    @Transactional
    public PosSaleResponseDTO cancelSale(UUID storeId, UUID saleId) {
        validateRole(storeId);
        PosSale sale = findSale(storeId, saleId);

        if (sale.getStatus() == PosSaleStatus.CANCELLED) {
            throw new IllegalArgumentException("La venta ya está cancelada");
        }

        sale.setStatus(PosSaleStatus.CANCELLED);
        sale.setCancelledAt(LocalDateTime.now());
        PosSale saved = saleRepository.save(sale);
        log.info("Venta POS cancelada: {}", sale.getSaleNumber());

        List<Map<String, Object>> movements = sale.getItems().stream()
                .filter(item -> item.getVariantId() != null)
                .map(item -> InventoryClient.movement(item.getVariantId(), item.getQuantity(), "IN"))
                .toList();
        inventoryClient.registerMovementsBatch(storeId, movements);

        return mapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public DailySummaryResponseDTO getDailySummary(UUID storeId) {
        validateRole(storeId);
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.atStartOfDay();
        LocalDateTime to = today.atTime(23, 59, 59);

        List<PosSale> sales = saleRepository
                .findByStoreIdAndCreatedAtBetweenOrderByCreatedAtDesc(storeId, from, to, Pageable.unpaged())
                .getContent();

        List<PosSale> completed = sales.stream()
                .filter(s -> s.getStatus() == PosSaleStatus.COMPLETED)
                .toList();

        BigDecimal totalRevenue = completed.stream()
                .map(PosSale::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDiscount = completed.stream()
                .map(PosSale::getDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long cancelledCount = sales.stream()
                .filter(s -> s.getStatus() == PosSaleStatus.CANCELLED).count();

        BigDecimal averageSale = completed.isEmpty()
                ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(completed.size()), 2, RoundingMode.HALF_UP);

        return DailySummaryResponseDTO.builder()
                .date(today)
                .totalSales(completed.size())
                .cancelledSales(cancelledCount)
                .totalRevenue(totalRevenue)
                .totalDiscount(totalDiscount)
                .averageSale(averageSale)
                .build();
    }

    private PosSale findSale(UUID storeId, UUID saleId) {
        return saleRepository.findBySaleIdAndStoreId(saleId, storeId)
                .orElseThrow(() -> new PosSaleNotFoundException("Venta no encontrada: " + saleId));
    }

    /**
     * Valida el rol del usuario EN ESTA TIENDA específica (vía Store), no el rol
     * global de la plataforma del JWT (X-User-Role) — un OWNER de tienda normalmente
     * tiene rol global "USER", así que validar contra X-User-Role bloqueaba a
     * cualquier dueño de tienda real.
     */
    private void validateRole(UUID storeId) {
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new ForbiddenException("No tienes permiso para acceder al módulo POS");
        }
        UUID userId;
        try {
            userId = UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            throw new ForbiddenException("No tienes permiso para acceder al módulo POS");
        }

        String role = storeClient.userRole(userId, storeId);
        if (role == null || !ALLOWED_ROLES.contains(role.toUpperCase())) {
            throw new ForbiddenException("No tienes permiso para acceder al módulo POS");
        }
    }

    private String generateSaleNumber(UUID storeId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long count = saleRepository.countByStoreIdAndCreatedAtGreaterThanEqual(storeId, startOfDay);
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String seq = String.format("%03d", count + 1);
        String candidate = "POS-" + date + "-" + seq;

        while (saleRepository.existsBySaleNumber(candidate)) {
            count++;
            seq = String.format("%03d", count + 1);
            candidate = "POS-" + date + "-" + seq;
        }
        return candidate;
    }
}
