package com.api.OrderPayment.controller;

import com.api.OrderPayment.dto.order.CreateOrderRequestDTO;
import com.api.OrderPayment.dto.order.OrderResponseDTO;
import com.api.OrderPayment.dto.order.UpdateOrderStatusRequestDTO;
import com.api.OrderPayment.enums.OrderStatus;
import com.api.OrderPayment.service.OrderService;
import com.api.OrderPayment.util.HeaderUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
 * POST   /api/v1/stores/{storeId}/orders                      → Crear orden desde carrito
 * GET    /api/v1/stores/{storeId}/orders                      → Listar mis órdenes
 * GET    /api/v1/stores/{storeId}/orders/{orderId}            → Ver detalle de una orden
 * DELETE /api/v1/stores/{storeId}/orders/{orderId}            → Cancelar una orden
 *
 * (Admin)
 * GET    /api/v1/stores/{storeId}/orders/admin/all            → Listar todas las órdenes de la tienda
 * PATCH  /api/v1/stores/{storeId}/orders/{orderId}/status     → Actualizar estado de orden
 */
@RestController
@RequestMapping("/stores/{storeId}/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final HeaderUtil headerUtil;

    /** Crear una orden desde el carrito activo del usuario */
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @PathVariable UUID storeId,
            @Valid @RequestBody(required = false) CreateOrderRequestDTO dto) {

        UUID userId = headerUtil.requireUserId();
        if (dto == null) dto = new CreateOrderRequestDTO();
        OrderResponseDTO response = orderService.createOrderFromCart(storeId, userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Listar las órdenes del usuario en esta tienda */
    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getMyOrders(@PathVariable UUID storeId) {
        UUID userId = headerUtil.requireUserId();
        return ResponseEntity.ok(orderService.getOrdersByUser(storeId, userId));
    }

    /** Ver el detalle de una orden */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrder(
            @PathVariable UUID storeId,
            @PathVariable UUID orderId) {

        UUID userId = headerUtil.requireUserId();
        return ResponseEntity.ok(orderService.getOrderById(orderId, userId));
    }

    /** Cancelar una orden (solo PENDING o CONFIRMED) */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable UUID storeId,
            @PathVariable UUID orderId) {

        UUID userId = headerUtil.requireUserId();
        orderService.cancelOrder(orderId, userId);
        return ResponseEntity.noContent().build();
    }

    // ─── Endpoints de administración ───────────────────────────────────────────

    /** [Interno] Endpoint para microservicios (Reports). Devuelve lista completa sin paginación. */
    @GetMapping("/admin/internal")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrdersByStoreInternal(@PathVariable UUID storeId) {
        return ResponseEntity.ok(orderService.getOrdersByStoreInternal(storeId));
    }

    /** [Admin] Listar todas las órdenes de la tienda con paginación y filtro opcional por status */
    @GetMapping("/admin/all")
    public ResponseEntity<Page<OrderResponseDTO>> getAllOrdersByStore(
            @PathVariable UUID storeId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(orderService.getOrdersByStore(storeId, status, page, size));
    }

    /** [Admin] Actualizar el estado de una orden */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable UUID storeId,
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequestDTO dto) {

        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, dto));
    }

}
