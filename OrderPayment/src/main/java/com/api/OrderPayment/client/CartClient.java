package com.api.OrderPayment.client;

import com.api.OrderPayment.client.dto.CartResponseDTO;
import com.api.OrderPayment.exception.CartServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Cliente HTTP para comunicarse con el módulo de Carrito (puerto 8086).
 *
 * Endpoints consumidos:
 *   GET    /api/stores/{storeId}/cart         → obtener carrito activo
 *   DELETE /api/stores/{storeId}/cart         → vaciar carrito tras crear orden
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CartClient {

    private final WebClient webClient;

    @Value("${cart.service.base-url}")
    private String cartBaseUrl;

    /**
     * Obtiene el carrito activo del usuario para una tienda.
     * Propaga el header x-user-id al servicio de carrito.
     */
    public CartResponseDTO getCart(UUID storeId, UUID userId) {
        log.info("Consultando carrito en Cart-Service: storeId={}, userId={}", storeId, userId);

        return webClient.get()
                .uri(cartBaseUrl + "/stores/{storeId}/cart", storeId)
                .header("x-user-id", userId.toString())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new CartServiceException(
                                        "Error al consultar el carrito (4xx): " + body))))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new CartServiceException(
                                "El servicio de carrito no está disponible (5xx)")))
                .bodyToMono(CartResponseDTO.class)
                .block();
    }

    /**
     * Vacía el carrito del usuario tras crear la orden exitosamente.
     */
    public void clearCart(UUID storeId, UUID userId) {
        log.info("Vaciando carrito en Cart-Service: storeId={}, userId={}", storeId, userId);

        webClient.delete()
                .uri(cartBaseUrl + "/stores/{storeId}/cart", storeId)
                .header("x-user-id", userId.toString())
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new CartServiceException(
                                "No se pudo vaciar el carrito tras crear la orden")))
                .bodyToMono(Void.class)
                .block();
    }
}
