package com.api.PosSale.service;

import com.api.PosSale.client.InventoryClient;
import com.api.PosSale.dto.CreatePosSaleRequestDTO;
import com.api.PosSale.dto.DailySummaryResponseDTO;
import com.api.PosSale.dto.PosSaleResponseDTO;
import com.api.PosSale.entity.PosSale;
import com.api.PosSale.entity.PosSaleItem;
import com.api.PosSale.enums.PosSaleStatus;
import com.api.PosSale.exception.PosSaleNotFoundException;
import com.api.PosSale.repository.PosSaleRepository;
import com.api.PosSale.util.PosSaleMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class PosSaleService {

    private final PosSaleRepository saleRepository;
    private final InventoryClient inventoryClient;
    private final PosSaleMapper mapper;

    private final AtomicLong saleCounter = new AtomicLong(1);

    @PostConstruct
    void initCounter() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long today = saleRepository.countByCreatedAtGreaterThanEqual(startOfDay);
        saleCounter.set(today + 1);
    }

    @Transactional
    public PosSaleResponseDTO createSale(UUID storeId, UUID employeeId, CreatePosSaleRequestDTO dto) {
        log.info("Creando venta POS: storeId={}, employee={}", storeId, employeeId);

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

        BigDecimal amountReceived = dto.getAmountReceived() != null ? dto.getAmountReceived() : total;
        BigDecimal change = amountReceived.subtract(total).max(BigDecimal.ZERO);

        PosSale sale = PosSale.builder()
                .storeId(storeId)
                .employeeId(employeeId)
                .customerId(dto.getCustomerId())
                .saleNumber(generateSaleNumber())
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

        // Notificar al servicio de inventario (fallo tolerado para no bloquear la venta)
        items.forEach(item -> {
            if (item.getVariantId() != null) {
                inventoryClient.registerOutMovement(storeId, item.getVariantId(), item.getQuantity());
            }
        });

        return mapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<PosSaleResponseDTO> getSalesByStore(UUID storeId) {
        return saleRepository.findByStoreIdOrderByCreatedAtDesc(storeId)
                .stream().map(mapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public PosSaleResponseDTO getSale(UUID storeId, UUID saleId) {
        return mapper.toDTO(findSale(storeId, saleId));
    }

    @Transactional(readOnly = true)
    public List<PosSaleResponseDTO> getSalesByCustomer(UUID storeId, UUID customerId) {
        return saleRepository.findByStoreIdAndCustomerIdOrderByCreatedAtDesc(storeId, customerId)
                .stream().map(mapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<PosSaleResponseDTO> getSalesByDateRange(UUID storeId, LocalDateTime from, LocalDateTime to) {
        return saleRepository.findByStoreIdAndCreatedAtBetweenOrderByCreatedAtDesc(storeId, from, to)
                .stream().map(mapper::toDTO).toList();
    }

    @Transactional
    public PosSaleResponseDTO cancelSale(UUID storeId, UUID saleId) {
        PosSale sale = findSale(storeId, saleId);

        if (sale.getStatus() != PosSaleStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Solo se pueden cancelar ventas en estado COMPLETED. Estado actual: " + sale.getStatus());
        }

        sale.setStatus(PosSaleStatus.CANCELLED);
        log.info("Venta POS cancelada: {}", sale.getSaleNumber());
        return mapper.toDTO(saleRepository.save(sale));
    }

    @Transactional(readOnly = true)
    public DailySummaryResponseDTO getDailySummary(UUID storeId) {
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.atStartOfDay();
        LocalDateTime to = today.atTime(23, 59, 59);

        List<PosSale> sales = saleRepository
                .findByStoreIdAndCreatedAtBetweenOrderByCreatedAtDesc(storeId, from, to);

        BigDecimal totalRevenue = sales.stream()
                .filter(s -> s.getStatus() == PosSaleStatus.COMPLETED)
                .map(PosSale::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDiscount = sales.stream()
                .filter(s -> s.getStatus() == PosSaleStatus.COMPLETED)
                .map(PosSale::getDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completed = sales.stream().filter(s -> s.getStatus() == PosSaleStatus.COMPLETED).count();
        long cancelled = sales.stream().filter(s -> s.getStatus() == PosSaleStatus.CANCELLED).count();

        return DailySummaryResponseDTO.builder()
                .date(today)
                .totalSales(completed)
                .cancelledSales(cancelled)
                .totalRevenue(totalRevenue)
                .totalDiscount(totalDiscount)
                .build();
    }

    private PosSale findSale(UUID storeId, UUID saleId) {
        return saleRepository.findBySaleIdAndStoreId(saleId, storeId)
                .orElseThrow(() -> new PosSaleNotFoundException("Venta no encontrada: " + saleId));
    }

    private String generateSaleNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seq = String.format("%06d", saleCounter.getAndIncrement());
        String candidate = "POS-" + date + "-" + seq;

        while (saleRepository.existsBySaleNumber(candidate)) {
            seq = String.format("%06d", saleCounter.getAndIncrement());
            candidate = "POS-" + date + "-" + seq;
        }
        return candidate;
    }
}
