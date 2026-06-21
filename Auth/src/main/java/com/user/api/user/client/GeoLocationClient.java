package com.user.api.user.client;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

/**
 * Geolocalización aproximada por IP usando ip-api.com (free tier, sin API key).
 * Solo para mostrar "ciudad, país" en el correo de alerta de login — no es
 * precisión de grado legal/forense.
 */
@Slf4j
@Component
public class GeoLocationClient {

    private static final Pattern PRIVATE_IP = Pattern.compile(
            "^(127\\.|10\\.|192\\.168\\.|172\\.(1[6-9]|2\\d|3[0-1])\\.|::1|0:0:0:0:0:0:0:1)");

    private final WebClient geoWebClient;

    public GeoLocationClient(@Qualifier("geoWebClient") WebClient geoWebClient) {
        this.geoWebClient = geoWebClient;
    }

    public Optional<String> resolveLocation(String ip) {
        if (ip == null || ip.isBlank() || PRIVATE_IP.matcher(ip).find()) {
            return Optional.empty();
        }

        try {
            Map<String, Object> response = geoWebClient.get()
                    .uri("/json/{ip}?fields=status,city,regionName,country", ip)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(Duration.ofSeconds(2));

            if (response == null || !"success".equals(response.get("status"))) {
                return Optional.empty();
            }

            String city = (String) response.get("city");
            String region = (String) response.get("regionName");
            String country = (String) response.get("country");

            StringBuilder sb = new StringBuilder();
            if (city != null && !city.isBlank()) sb.append(city);
            if (region != null && !region.isBlank() && !region.equals(city)) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(region);
            }
            if (country != null && !country.isBlank()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(country);
            }

            return sb.length() > 0 ? Optional.of(sb.toString()) : Optional.empty();
        } catch (Exception e) {
            log.warn("No se pudo resolver ubicación para IP {}: {}", ip, e.getMessage());
            return Optional.empty();
        }
    }
}
