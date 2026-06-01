package com.api.payments.service;

import com.api.payments.config.MercadoPagoConfiguration;
import com.api.payments.dto.request.CheckoutProRequest;
import com.api.payments.dto.response.PaymentResponse;
import com.api.payments.entity.CommissionRecord;
import com.api.payments.entity.PaymentTransaction;
import com.api.payments.enums.PaymentStatus;
import com.api.payments.enums.PaymentType;
import com.api.payments.exception.PaymentException;
import com.api.payments.repository.CommissionRecordRepository;
import com.api.payments.repository.PaymentTransactionRepository;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketplaceService {

    private final PaymentTransactionRepository transactionRepo;
    private final CommissionRecordRepository commissionRepo;
    private final OAuthService oauthService;
    private final MercadoPagoConfiguration mpConfig;

    /**
     * Crea una preferencia de Checkout Pro con split automático.
     *
     * Mercado Pago Marketplace permite que al procesar el pago:
     *   - El dinero va a la cuenta del tenant (collector).
     *   - Automáticamente se descuenta el marketplace_fee hacia tu cuenta.
     *
     * Prerequisito: el tenant debe haber completado el flujo OAuth.
     */
    @Transactional
    public PaymentResponse createCheckoutPro(CheckoutProRequest request) {
        log.info("Creando Checkout Pro para tenant={} ref={}", request.getTenantId(), request.getExternalReference());

        // Obtener access_token del tenant (para cobrar en su nombre)
        String tenantAccessToken = oauthService.getValidAccessToken(request.getTenantId());

        // Calcular monto total
        BigDecimal totalAmount = request.getItems().stream()
            .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcular comisión de la plataforma (1%)
        BigDecimal platformFee = totalAmount
            .multiply(BigDecimal.valueOf(mpConfig.getPlatformFeeRatio()))
            .setScale(2, RoundingMode.HALF_UP);

        BigDecimal netAmount = totalAmount.subtract(platformFee);

        try {
            // Construir ítems de la preferencia
            List<PreferenceItemRequest> items = request.getItems().stream()
                .map(i -> PreferenceItemRequest.builder()
                    .title(i.getTitle())
                    .description(i.getDescription())
                    .quantity(i.getQuantity())
                    .unitPrice(i.getUnitPrice())
                    .currencyId(i.getCurrencyId())
                    .pictureUrl(i.getPictureUrl())
                    .build())
                .collect(Collectors.toList());

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(mpConfig.getFrontendUrl() + "/store/checkout/success")
                .failure(mpConfig.getFrontendUrl() + "/store/checkout/failure")
                .pending(mpConfig.getFrontendUrl() + "/store/checkout/pending")
                .build();

            PreferenceRequest preferenceReq = PreferenceRequest.builder()
                .items(items)
                .backUrls(backUrls)
                .autoReturn("approved")
                .externalReference(request.getExternalReference())
                .notificationUrl(mpConfig.getNotificationUrl())
                // marketplace_fee: lo que tú cobras (en centavos o en la moneda configurada)
                .marketplaceFee(platformFee)
                .build();

            // IMPORTANTE: usar el access_token del TENANT como collector
            // El SDK de MP permite pasar el token por request usando el header personalizado
            PreferenceClient client = new PreferenceClient();

            // Configurar el access token del tenant para esta petición
            com.mercadopago.core.MPRequestOptions options = com.mercadopago.core.MPRequestOptions.builder()
                .accessToken(tenantAccessToken)
                .build();

            Preference preference = client.create(preferenceReq, options);

            // Guardar transacción en estado PENDING
            PaymentTransaction tx = PaymentTransaction.builder()
                .tenantId(request.getTenantId())
                .mpPreferenceId(preference.getId())
                .paymentType(PaymentType.STORE_SALE_PRO)
                .status(PaymentStatus.PENDING)
                .amount(totalAmount)
                .platformFee(platformFee)
                .netAmount(netAmount)
                .currency("COP")
                .externalReference(request.getExternalReference())
                .description("Venta tienda: " + request.getItems().get(0).getTitle())
                .payerEmail(request.getPayerEmail())
                .build();

            transactionRepo.save(tx);

            return PaymentResponse.builder()
                .id(tx.getId())
                .tenantId(request.getTenantId())
                .mpPreferenceId(preference.getId())
                .paymentType(PaymentType.STORE_SALE_PRO)
                .status(PaymentStatus.PENDING)
                .amount(totalAmount)
                .platformFee(platformFee)
                .netAmount(netAmount)
                .currency("COP")
                .externalReference(request.getExternalReference())
                .checkoutUrl(preference.getInitPoint())
                .sandboxCheckoutUrl(preference.getSandboxInitPoint())
                .build();

        } catch (MPApiException e) {
            log.error("Error MP API Checkout Pro: status={} content={}", e.getStatusCode(), e.getApiResponse().getContent());
            throw new PaymentException("Error de Mercado Pago al crear preferencia: " + e.getMessage(), e);
        } catch (MPException e) {
            log.error("Error MP Checkout Pro", e);
            throw new PaymentException("Error al conectar con Mercado Pago", e);
        }
    }

    /**
     * Registra la comisión una vez que el pago es aprobado.
     * Llamado desde el WebhookProcessor cuando status = approved.
     */
    @Transactional
    public void recordCommission(PaymentTransaction tx) {
        if (tx.getPlatformFee() == null || tx.getPlatformFee().compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        CommissionRecord commission = CommissionRecord.builder()
            .tenantId(tx.getTenantId())
            .paymentTransactionId(tx.getId())
            .mpPaymentId(tx.getMpPaymentId())
            .saleAmount(tx.getAmount())
            .commissionRate(BigDecimal.valueOf(mpConfig.getPlatformFeeRatio()))
            .commissionAmount(tx.getPlatformFee())
            .currency(tx.getCurrency())
            .build();

        commissionRepo.save(commission);
        log.info("Comisión registrada: tenant={} monto={} comision={}", tx.getTenantId(), tx.getAmount(), tx.getPlatformFee());
    }
}
