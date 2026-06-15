package com.api.product.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.api.product.dto.StockAlertDTO;

@Service
public class StockAlertService {

    private final Map<UUID, List<SseEmitter>> emittersByStore = new ConcurrentHashMap<>();

    public SseEmitter subscribe(UUID storeId) {
        SseEmitter emitter = new SseEmitter(0L);

        emittersByStore.computeIfAbsent(storeId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        Runnable cleanup = () -> removeEmitter(storeId, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> removeEmitter(storeId, emitter));

        return emitter;
    }

    public void sendAlert(StockAlertDTO alert) {
        List<SseEmitter> emitters = emittersByStore.get(alert.getStoreId());
        if (emitters == null || emitters.isEmpty()) return;

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("stock-alert")
                        .data(alert));
            } catch (IOException e) {
                removeEmitter(alert.getStoreId(), emitter);
            }
        }
    }

    private void removeEmitter(UUID storeId, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersByStore.get(storeId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) emittersByStore.remove(storeId);
        }
    }
}
