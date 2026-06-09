package com.api.PosSale.entity;

import com.api.PosSale.enums.PosPaymentMethod;
import com.api.PosSale.enums.PosSaleStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pos_sales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosSale {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "sale_id")
    private UUID saleId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    /** ID del empleado que registra la venta (del header X-User-Id) */
    @Column(name = "employee_id")
    private UUID employeeId;

    /** Cliente opcional (puede ser venta anónima) */
    @Column(name = "customer_id")
    private UUID customerId;

    /** Número legible: POS-20240101-000001 */
    @Column(name = "sale_number", nullable = false, unique = true)
    private String saleNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PosSaleStatus status = PosSaleStatus.COMPLETED;

    @Builder.Default
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PosSaleItem> items = new ArrayList<>();

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PosPaymentMethod paymentMethod;

    /** Monto recibido (útil para calcular el cambio en efectivo) */
    @Column(name = "amount_received", precision = 12, scale = 2)
    private BigDecimal amountReceived;

    /** Cambio devuelto al cliente */
    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal change = BigDecimal.ZERO;

    @Column(length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
