package com.api.product.client;

import com.api.product.dto.CloudinarySignatureResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Map;

@Component
public class MediaServiceClient {

    private final WebClient webClient;

    public MediaServiceClient(WebClient.Builder builder,
                              @Value("${media.service.url:http://localhost:8096}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public String upload(MultipartFile file, String folder) {
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo: " + e.getMessage());
        }

        ByteArrayResource resource = new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("image", resource);

        Map<?, ?> response = webClient.post()
                .uri("/api/v1/upload?folder=" + folder)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(formData))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || !response.containsKey("url")) {
            throw new RuntimeException("El media-service no devolvió una URL válida");
        }

        return (String) response.get("url");
    }

    public CloudinarySignatureResponse getSignature(String folder) {
        Map<?, ?> response = webClient.get()
                .uri("/api/v1/upload/signature?folder=" + folder)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null) {
            throw new RuntimeException("El media-service no devolvió la firma");
        }

        return new CloudinarySignatureResponse(
                (String) response.get("signature"),
                ((Number) response.get("timestamp")).longValue(),
                (String) response.get("apiKey"),
                (String) response.get("cloudName"),
                (String) response.get("folder")
        );
    }
}
