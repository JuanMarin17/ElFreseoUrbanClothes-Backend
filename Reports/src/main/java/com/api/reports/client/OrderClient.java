package com.api.reports.client;

import com.api.reports.dto.external.ExternalOrderDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderClient {

    private final WebClient orderWebClient;

    public List<ExternalOrderDTO> getAllOrders(UUID storeId) {
        try {
            List<ExternalOrderDTO> orders = orderWebClient.get()
                    .uri("/stores/{storeId}/orders/admin/internal", storeId)
                    .header("X-Store-Id", storeId.toString())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<ExternalOrderDTO>>() {})
                    .block();

            return orders != null ? orders : Collections.emptyList();

        } catch (Exception e) {
            log.warn("No se pudo obtener órdenes del OrderPayment service para storeId={}: {}", storeId, e.getMessage());
            return Collections.emptyList();
        }
    }
}
