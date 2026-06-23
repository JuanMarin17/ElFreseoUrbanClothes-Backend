package com.api.reports.client;

import com.api.reports.cache.TtlCache;
import com.api.reports.dto.external.ExternalOrderDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderClient {

    private final WebClient orderWebClient;

    // Evita repetir la misma llamada pesada cuando el panel admin consulta varios reportes en segundos.
    private final TtlCache<UUID, List<ExternalOrderDTO>> cache = new TtlCache<>(Duration.ofSeconds(30));

    public List<ExternalOrderDTO> getAllOrders(UUID storeId) {
        return cache.getOrCompute(storeId, () -> fetchAllOrders(storeId));
    }

    private List<ExternalOrderDTO> fetchAllOrders(UUID storeId) {
        try {
            List<ExternalOrderDTO> orders = orderWebClient.get()
                    .uri("/stores/{storeId}/orders/admin/internal", storeId)
                    .header("X-Store-Id", storeId.toString())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<ExternalOrderDTO>>() {})
                    .timeout(Duration.ofSeconds(10))
                    .block();

            return orders != null ? orders : Collections.emptyList();

        } catch (Exception e) {
            log.warn("No se pudo obtener órdenes del OrderPayment service para storeId={}: {}", storeId, e.getMessage());
            return Collections.emptyList();
        }
    }
}
