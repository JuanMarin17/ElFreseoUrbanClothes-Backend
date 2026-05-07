package com.api.product.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.api.product.dto.StockAlertDTO;

/**
 * Servicio encargado de gestionar las alertas de stock en tiempo real usando SSE (Server-Sent Events).
 * <p>
 * Este servicio mantiene una lista de conexiones activas (clientes conectados) mediante {@link SseEmitter},
 * permitiendo enviar notificaciones automáticamente al frontend cuando se detecta que un producto o variante
 * ha llegado o bajado del stock mínimo.
 *
 * <p>
 * El flujo normal es:
 * <ul>
 *   <li>El frontend se conecta al endpoint SSE y queda escuchando.</li>
 *   <li>El backend guarda el {@link SseEmitter} en memoria.</li>
 *   <li>Cuando ocurre un evento (stock bajo), se llama a {@link #sendAlert(StockAlertDTO)}.</li>
 *   <li>El servicio envía el evento a todos los clientes conectados.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Se usa {@link CopyOnWriteArrayList} para evitar problemas de concurrencia, ya que varios clientes
 * pueden conectarse y desconectarse simultáneamente mientras se envían eventos.
 * </p>
 */
@Service
public class StockAlertService {

    /**
     * Lista de clientes conectados mediante SSE.
     * Cada elemento representa una conexión activa hacia el frontend.
     */
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /**
     * Registra una nueva conexión SSE.
     * <p>
     * Cuando un cliente se conecta, se crea un {@link SseEmitter} sin timeout
     * y se guarda en la lista de emisores activos.
     * </p>
     *
     * <p>
     * Además se configuran listeners para eliminar el emitter cuando:
     * <ul>
     *   <li>La conexión se completa</li>
     *   <li>La conexión expira</li>
     *   <li>Ocurre un error</li>
     * </ul>
     * </p>
     *
     * @return SseEmitter conexión SSE activa para el cliente
     */
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L); // sin timeout
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        return emitter;
    }

    /**
     * Envía una alerta de stock a todos los clientes conectados.
     * <p>
     * Este método recorre todos los {@link SseEmitter} activos y envía un evento SSE
     * con nombre "stock-alert" y el contenido del {@link StockAlertDTO}.
     * </p>
     *
     * <p>
     * Si algún cliente ya no está disponible o ocurre un error al enviar el evento,
     * el emitter se elimina automáticamente de la lista.
     * </p>
     *
     * @param alert DTO que contiene la información de la alerta de stock bajo
     */
    public void sendAlert(StockAlertDTO alert) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("stock-alert")
                        .data(alert));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}