package com.api.OrderPayment.client;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.api.OrderPayment.client.dto.CouponValidationDTO;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PromotionClient {

    private final RestClient restClient;

    public PromotionClient(@Value("${promotion.service.url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Valida un cupón sin registrar redención.
     * Devuelve empty si el cupón no existe, está inactivo o ya fue usado.
     */
    public Optional<CouponValidationDTO> validateCoupon(String code, UUID storeId, UUID userId) {
        try {
            CouponValidationDTO result = restClient.get()
                    .uri("/promotions/internal/coupons/validate/{code}", code)
                    .header("X-Store-Id", storeId.toString())
                    .header("X-User-Id", userId.toString())
                    .retrieve()
                    .body(CouponValidationDTO.class);
            return Optional.ofNullable(result);
        } catch (HttpClientErrorException.NotFound | HttpClientErrorException.BadRequest e) {
            log.warn("Cupón inválido '{}': {}", code, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("No se pudo validar el cupón '{}': {}", code, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Registra la redención del cupón tras crear la orden exitosamente.
     */
    public void redeemCoupon(String code, UUID storeId, UUID userId) {
        try {
            restClient.post()
                    .uri("/coupons/redeem/{code}", code)
                    .header("X-Store-Id", storeId.toString())
                    .header("X-User-Id", userId.toString())
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("No se pudo registrar redención del cupón '{}': {}", code, e.getMessage());
        }
    }
}
