package com.api.OrderPayment.controller;

import com.api.OrderPayment.dto.order.CreateOrderRequestDTO;
import com.api.OrderPayment.dto.order.OrderResponseDTO;
import com.api.OrderPayment.dto.order.UpdateOrderStatusRequestDTO;
import com.api.OrderPayment.service.OrderService;
import com.api.OrderPayment.util.HeaderUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Gestión de órdenes de compra para una tienda.
 *
 * El userId se lee del header "x-user-id" (UUID).
 * En producción este header lo inyecta el gateway de autenticación.
 *
 * POST   /api/stores/{storeId}/orders                      → Crear orden desde carrito
 * GET    /api/stores/{storeId}/orders                      → Listar mis órdenes
 * GET    /api/stores/{storeId}/orders/{orderId}            → Ver detalle de una orden
 * DELETE /api/stores/{storeId}/orders/{orderId}            → Cancelar una orden
 *
 * (Admin)
 * GET    /api/stores/{storeId}/orders/admin/all            → Listar todas las órdenes de la tienda
 * PATCH  /api/stores/{storeId}/orders/{orderId}/status     → Actualizar estado de orden
 */
@RestController
@RequestMapping("/api/stores/{storeId}/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final HeaderUtil headerUtil;

    /** Crear una orden desde el carrito activo del usuario */
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @PathVariable UUID storeId,
            @Valid @RequestBody(required = false) CreateOrderRequestDTO dto) {

        UUID userId = requireUserId();
        if (dto == null) dto = new CreateOrderRequestDTO();
        OrderResponseDTO response = orderService.createOrderFromCart(storeId, userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Listar las órdenes del usuario en esta tienda */
    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getMyOrders(@PathVariable UUID storeId) {
        UUID userId = requireUserId();
        return ResponseEntity.ok(orderService.getOrdersByUser(storeId, userId));
    }

    /** Ver el detalle de una orden */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrder(
            @PathVariable UUID storeId,
            @PathVariable UUID orderId) {

        UUID userId = requireUserId();
        return ResponseEntity.ok(orderService.getOrderById(orderId, userId));
    }

    /** Cancelar una orden (solo PENDING o CONFIRMED) */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> cancelOrder(
            @PathVariable UUID storeId,
            @PathVariable UUID orderId) {

        UUID userId = requireUserId();
        return ResponseEntity.ok(orderService.cancelOrder(orderId, userId));
    }

    // ─── Endpoints de administración ───────────────────────────────────────────

    /** [Admin] Listar todas las órdenes de la tienda */
    @GetMapping("/admin/all")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrdersByStore(@PathVariable UUID storeId) {
        return ResponseEntity.ok(orderService.getOrdersByStore(storeId));
    }

    /** [Admin] Actualizar el estado de una orden */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable UUID storeId,
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequestDTO dto) {

        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, dto));
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private UUID requireUserId() {
        return headerUtil.getUserIdFromHeader()
                .orElseThrow(() -> new IllegalArgumentException(
                        "El header 'x-user-id' es obligatorio"));
    }
}
