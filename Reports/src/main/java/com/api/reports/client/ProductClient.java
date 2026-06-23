package com.api.reports.client;

import com.api.reports.cache.TtlCache;
import com.api.reports.dto.external.ExternalProductDTO;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductClient {

    private final WebClient productWebClient;

    // Evita repetir la misma llamada pesada cuando el panel admin consulta varios reportes en segundos.
    private final TtlCache<String, List<ExternalProductDTO>> cache = new TtlCache<>(Duration.ofSeconds(30));

    public List<ExternalProductDTO> getAllProducts(String storeId) {
        return cache.getOrCompute(storeId, () -> fetchAllProducts(storeId));
    }

    private List<ExternalProductDTO> fetchAllProducts(String storeId) {
        try {
            JsonNode response = productWebClient.get()
                    .uri("/products/all")
                    .header("X-Store-Id", storeId)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response == null || !response.has("data") || response.get("data").isNull()) {
                return Collections.emptyList();
            }

            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .findAndRegisterModules()
                    .readerForListOf(ExternalProductDTO.class)
                    .readValue(response.get("data"));

        } catch (Exception e) {
            log.warn("No se pudo obtener productos del Product service: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
