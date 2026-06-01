package com.api.reports.client;

import com.api.reports.dto.external.ExternalProductDTO;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductClient {

    private final WebClient productWebClient;

    public List<ExternalProductDTO> getAllProducts(String storeId) {
        try {
            JsonNode response = productWebClient.get()
                    .uri("/products/all")
                    .header("X-Store-Id", storeId)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
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
