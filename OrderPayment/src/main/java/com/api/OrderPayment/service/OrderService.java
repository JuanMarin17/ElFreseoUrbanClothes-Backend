package com.api.OrderPayment.service;

import com.api.OrderPayment.dto.order.CreateOrderRequestDTO;
import com.api.OrderPayment.dto.order.OrderResponseDTO;
import com.api.OrderPayment.dto.order.UpdateOrderStatusRequestDTO;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    /**
     * Crea una orden a partir del carrito activo del usuario.
     * Consulta el módulo Cart (8086), genera la orden y vacía el carrito.
     */
    OrderResponseDTO createOrderFromCart(UUID storeId, UUID userId, CreateOrderRequestDTO dto);

    /** Obtiene una orden por su ID */
    OrderResponseDTO getOrderById(UUID orderId, UUID userId);

    /** Lista todas las órdenes del usuario en una tienda */
    List<OrderResponseDTO> getOrdersByUser(UUID storeId, UUID userId);

    /** Lista todas las órdenes de una tienda (uso interno / admin) */
    List<OrderResponseDTO> getOrdersByStore(UUID storeId);

    /** Actualiza el estado de una orden */
    OrderResponseDTO updateOrderStatus(UUID orderId, UpdateOrderStatusRequestDTO dto);

    /** Cancela una orden (solo si está en PENDING o CONFIRMED) */
    OrderResponseDTO cancelOrder(UUID orderId, UUID userId);
}
