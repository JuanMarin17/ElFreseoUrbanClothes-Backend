package com.api.payments.service;

import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.api.payments.config.MercadoPagoConfiguration;
import com.api.payments.dto.request.CheckoutApiRequest;
import com.api.payments.dto.response.PaymentResponse;
import com.api.payments.entity.PaymentTransaction;
import com.api.payments.enums.PaymentStatus;
import com.api.payments.enums.PaymentType;
import com.api.payments.exception.PaymentException;
import com.api.payments.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutApiService {

    private final PaymentTransactionRepository transactionRepo;
    private final MarketplaceService marketplaceService;
    private final OAuthService oauthService;
    private final MercadoPagoConfiguration mpConfig;

    /**
     * Procesa un pago con tarjeta directamente (Checkout API / tokenización).
     * El frontend usa MercadoPago.js para tokenizar la tarjeta y manda el
     * cardToken.
     * Nosotros procesamos el pago en nombre del tenant con split automático.
     */
    @Transactional
    public PaymentResponse processCardPayment(CheckoutApiRequest request) {
        log.info("Procesando pago con tarjeta tenant={} monto={}", request.getTenantId(), request.getAmount());

        String tenantAccessToken = oauthService.getValidAccessToken(request.getTenantId());

        BigDecimal platformFee = request.getAmount()
                .multiply(BigDecimal.valueOf(mpConfig.getPlatformFeeRatio()))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal netAmount = request.getAmount().subtract(platformFee);

        try {
            IdentificationRequest identification = null;
            if (request.getPayer().getIdentification() != null) {
                identification = IdentificationRequest.builder()
                        .type(request.getPayer().getIdentification().getType())
                        .number(request.getPayer().getIdentification().getNumber())
                        .build();
            }

            PaymentPayerRequest payer = PaymentPayerRequest.builder()
                    .email(request.getPayer().getEmail())
                    .firstName(request.getPayer().getFirstName())
                    .lastName(request.getPayer().getLastName())
                    .identification(identification)
                    .build();

            PaymentCreateRequest paymentReq = PaymentCreateRequest.builder()
                    .transactionAmount(request.getAmount())
                    .token(request.getCardToken())
                    .description(request.getDescription() != null ? request.getDescription() : "Compra en tienda")
                    .installments(request.getInstallments())
                    .paymentMethodId(request.getPaymentMethodId())
                    .payer(payer)
                    .externalReference(request.getExternalReference())
                    .notificationUrl(mpConfig.getNotificationUrl())
                    // El marketplace_fee se descuenta automáticamente hacia tu cuenta
                    .applicationFee(platformFee)
                    .build();

            // Procesar con el access_token del tenant (dinero va a su cuenta)
            PaymentClient client = new PaymentClient();
            com.mercadopago.core.MPRequestOptions options = com.mercadopago.core.MPRequestOptions.builder()
                    .accessToken(tenantAccessToken)
                    .build();

            Payment payment = client.create(paymentReq, options);

            PaymentStatus status = mapMpStatus(payment.getStatus());

            PaymentTransaction tx = PaymentTransaction.builder()
                    .tenantId(request.getTenantId())
                    .mpPaymentId(payment.getId().toString())
                    .paymentType(PaymentType.STORE_SALE_API)
                    .status(status)
                    .amount(request.getAmount())
                    .platformFee(platformFee)
                    .netAmount(netAmount)
                    .currency("COP")
                    .externalReference(request.getExternalReference())
                    .paymentMethodId(payment.getPaymentMethodId())
                    .paymentTypeId(payment.getPaymentTypeId())
                    .description(request.getDescription())
                    .payerEmail(request.getPayer().getEmail())
                    .build();

            if (status == PaymentStatus.APPROVED) {
                tx.setApprovedAt(java.time.LocalDateTime.now());
            }

            transactionRepo.save(tx);

            // Si fue aprobado, registrar comisión inmediatamente
            if (status == PaymentStatus.APPROVED) {
                marketplaceService.recordCommission(tx);
            }

            return PaymentResponse.builder()
                    .id(tx.getId())
                    .tenantId(request.getTenantId())
                    .mpPaymentId(payment.getId().toString())
                    .paymentType(PaymentType.STORE_SALE_API)
                    .status(status)
                    .amount(request.getAmount())
                    .platformFee(platformFee)
                    .netAmount(netAmount)
                    .currency("COP")
                    .externalReference(request.getExternalReference())
                    .paymentMethodId(payment.getPaymentMethodId())
                    .payerEmail(request.getPayer().getEmail())
                    .build();

        } catch (MPApiException e) {
            log.error("Error MP API pago tarjeta: status={} content={}", e.getStatusCode(),
                    e.getApiResponse().getContent());
            throw new PaymentException("Error al procesar pago: " + e.getMessage(), e);
        } catch (MPException e) {
            log.error("Error MP pago tarjeta", e);
            throw new PaymentException("Error al conectar con Mercado Pago", e);
        }
    }

    private PaymentStatus mapMpStatus(String mpStatus) {
        if (mpStatus == null)
            return PaymentStatus.PENDING;
        return switch (mpStatus.toLowerCase()) {
            case "approved" -> PaymentStatus.APPROVED;
            case "rejected" -> PaymentStatus.REJECTED;
            case "cancelled" -> PaymentStatus.CANCELLED;
            case "refunded" -> PaymentStatus.REFUNDED;
            case "in_process" -> PaymentStatus.IN_PROCESS;
            case "authorized" -> PaymentStatus.AUTHORIZED;
            default -> PaymentStatus.PENDING;
        };
    }
}
