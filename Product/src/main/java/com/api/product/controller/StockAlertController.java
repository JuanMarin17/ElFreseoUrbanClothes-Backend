package com.api.product.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.api.product.service.StockAlertService;

import lombok.RequiredArgsConstructor;

/**
 * Controlador encargado de exponer endpoints relacionados con alertas de stock.
 * <p>
 * Este controlador permite establecer una conexión SSE (Server-Sent Events)
 * para que el frontend pueda recibir notificaciones en tiempo real cuando
 * un producto o variante alcance o baje del stock mínimo definido.
 * </p>
 *
 * <p>
 * SSE mantiene una conexión abierta entre el cliente y el servidor, donde el servidor
 * puede enviar eventos automáticamente sin necesidad de que el cliente haga
 * peticiones repetidas (polling).
 * </p>
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockAlertController {

    private final StockAlertService stockAlertService;

    /**
     * Establece una conexión SSE para recibir alertas de stock en tiempo real.
     * Este endpoint debe ser consumido desde el frontend usando EventSource,
     * y permanecerá abierto para enviar eventos de tipo "stock-alert".
     * Ejemplo de consumo en frontend:
     * 
     * const eventSource = new EventSource("http://localhost:8081/stock/connect");
     * eventSource.onmessage = (event) => console.log(event.data);
     *
     * @return SseEmitter conexión abierta para transmisión de eventos SSE
     */
    @GetMapping("/alerts/stock/stream")
    public SseEmitter openStockAlertStream() {
        return stockAlertService.subscribe();
    }
}