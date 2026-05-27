package com.api.LoyalCustomer.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.LoyalCustomer.dto.ApiResponseDTO;
import com.api.LoyalCustomer.dto.EarnPointsRequestDTO;
import com.api.LoyalCustomer.dto.LedgerResponseDTO;
import com.api.LoyalCustomer.dto.LoyaltyAccountResponseDTO;
import com.api.LoyalCustomer.dto.RedeemPointsRequestDTO;
import com.api.LoyalCustomer.entity.LoyaltyAccount;
import com.api.LoyalCustomer.entity.LoyaltyLedger;
import com.api.LoyalCustomer.enums.LedgerType;
import com.api.LoyalCustomer.exception.BadRequestException;
import com.api.LoyalCustomer.exception.LoyaltyNotFoundException;
import com.api.LoyalCustomer.exception.UnauthorizedException;
import com.api.LoyalCustomer.repository.LoyaltyAccountRepository;
import com.api.LoyalCustomer.repository.LoyaltyLedgerRepository;
import com.common_request_context_starter.context.RequestContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final LoyaltyAccountRepository accountRepository;
    private final LoyaltyLedgerRepository ledgerRepository;

    @Value("${loyalty.points-per-amount}")
    private int pointsPerAmount;

    @Value("${loyalty.expiry-days}")
    private int expiryDays;

    // ── Obtener cuenta del usuario autenticado ────────────────────────────────
    public LoyaltyAccountResponseDTO getMyAccount() {
        UUID userId = getUserIdFromHeader();
        UUID storeId = getStoreIdFromHeader();

        LoyaltyAccount account = getOrCreateAccount(userId, storeId);
        expirePoints(account);
        return toAccountResponse(accountRepository.save(account));
    }

    // ── Ver historial de movimientos ──────────────────────────────────────────
    public List<LedgerResponseDTO> getMyLedger() {
        UUID userId = getUserIdFromHeader();
        UUID storeId = getStoreIdFromHeader();

        LoyaltyAccount account = getOrCreateAccount(userId, storeId);
        return ledgerRepository.findByAccountId(account.getAccountId())
                .stream().map(this::toLedgerResponse).toList();
    }

    // ── Ganar puntos (llamado internamente por Orders) ────────────────────────
    @Transactional
    public LoyaltyAccountResponseDTO earnPoints(EarnPointsRequestDTO dto) {
        if (dto.getUserId() == null || dto.getStoreId() == null || dto.getOrderTotal() == null)
            throw new BadRequestException("userId, storeId y orderTotal son obligatorios");

        int pointsEarned = dto.getOrderTotal()
                .divide(BigDecimal.valueOf(pointsPerAmount), 0, java.math.RoundingMode.DOWN)
                .intValue();

        if (pointsEarned <= 0)
            throw new BadRequestException("El total de la orden no es suficiente para ganar puntos");

        LoyaltyAccount account = getOrCreateAccount(dto.getUserId(), dto.getStoreId());
        expirePoints(account);

        account.setPoints(account.getPoints() + pointsEarned);
        LoyaltyAccount saved = accountRepository.save(account);

        LoyaltyLedger ledger = new LoyaltyLedger();
        ledger.setAccountId(saved.getAccountId());
        ledger.setPoints(pointsEarned);
        ledger.setType(LedgerType.EARN);
        ledger.setExpiresAt(OffsetDateTime.now().plusDays(expiryDays));
        ledgerRepository.save(ledger);

        return toAccountResponse(saved);
    }

    // ── Canjear puntos por descuento ──────────────────────────────────────────
    @Transactional
    public ApiResponseDTO redeemPoints(RedeemPointsRequestDTO dto) {
        UUID userId = getUserIdFromHeader();
        UUID storeId = getStoreIdFromHeader();

        if (dto.getPoints() == null || dto.getPoints() <= 0)
            throw new BadRequestException("La cantidad de puntos a canjear debe ser mayor a 0");

        LoyaltyAccount account = accountRepository.findByUserIdAndStoreId(userId, storeId)
                .orElseThrow(() -> new LoyaltyNotFoundException("No tienes una cuenta de puntos en esta tienda"));

        expirePoints(account);

        if (account.getPoints() < dto.getPoints())
            throw new BadRequestException("No tienes suficientes puntos. Tienes: " + account.getPoints());

        account.setPoints(account.getPoints() - dto.getPoints());
        accountRepository.save(account);

        LoyaltyLedger ledger = new LoyaltyLedger();
        ledger.setAccountId(account.getAccountId());
        ledger.setPoints(-dto.getPoints());
        ledger.setType(LedgerType.REDEEM);
        ledgerRepository.save(ledger);

        // 1 punto = $2.000 COP de descuento
        int discountValue = dto.getPoints() * pointsPerAmount;

        ApiResponseDTO response = new ApiResponseDTO();
        response.setMessage("Canjeaste " + dto.getPoints() + " puntos por un descuento de $" +
                discountValue + " COP");
        response.setStatus(200);
        return response;
    }

    // ── Ver cuenta de un usuario (ADMIN/OWNER) ────────────────────────────────
    public LoyaltyAccountResponseDTO getAccountByUser(UUID userId) {
        validateAdminOrOwner();
        UUID storeId = getStoreIdFromHeader();

        LoyaltyAccount account = accountRepository.findByUserIdAndStoreId(userId, storeId)
                .orElseThrow(() -> new LoyaltyNotFoundException(
                        "No se encontró cuenta de puntos para el usuario: " + userId));

        expirePoints(account);
        return toAccountResponse(accountRepository.save(account));
    }

    // ── Expirar puntos vencidos ───────────────────────────────────────────────
    private void expirePoints(LoyaltyAccount account) {
        List<LoyaltyLedger> expired = ledgerRepository
                .findExpiredEarnsByAccountId(account.getAccountId(), OffsetDateTime.now());

        if (expired.isEmpty())
            return;

        int totalExpired = expired.stream().mapToInt(LoyaltyLedger::getPoints).sum();

        int newPoints = Math.max(0, account.getPoints() - totalExpired);
        account.setPoints(newPoints);

        expired.forEach(l -> {
            LoyaltyLedger expireLedger = new LoyaltyLedger();
            expireLedger.setAccountId(account.getAccountId());
            expireLedger.setPoints(-l.getPoints());
            expireLedger.setType(LedgerType.EXPIRE);
            ledgerRepository.save(expireLedger);
            ledgerRepository.delete(l);
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private LoyaltyAccount getOrCreateAccount(UUID userId, UUID storeId) {
        return accountRepository.findByUserIdAndStoreId(userId, storeId)
                .orElseGet(() -> {
                    LoyaltyAccount account = new LoyaltyAccount();
                    account.setUserId(userId);
                    account.setStoreId(storeId);
                    account.setPoints(0);
                    return accountRepository.save(account);
                });
    }

    private UUID getUserIdFromHeader() {
        String userIdHeader = RequestContext.getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.isBlank())
            throw new UnauthorizedException("Usuario no autenticado");
        try {
            return UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Formato inválido del userId");
        }
    }

    private UUID getStoreIdFromHeader() {
        String storeIdHeader = RequestContext.getHeader("X-Store-Id");
        if (storeIdHeader == null || storeIdHeader.isBlank())
            throw new BadRequestException("No se encontró el X-Store-Id en el header");
        try {
            return UUID.fromString(storeIdHeader);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Formato inválido del storeId");
        }
    }

    private void validateAdminOrOwner() {
        String role = RequestContext.getHeader("X-User-Role");
        if (!"ADMIN".equals(role) && !"OWNER".equals(role))
            throw new UnauthorizedException("Solo el ADMIN u OWNER pueden realizar esta acción");
    }

    // ── Mappers ───────────────────────────────────────────────────────────────
    private LoyaltyAccountResponseDTO toAccountResponse(LoyaltyAccount a) {
        LoyaltyAccountResponseDTO dto = new LoyaltyAccountResponseDTO();
        dto.setAccountId(a.getAccountId());
        dto.setUserId(a.getUserId());
        dto.setStoreId(a.getStoreId());
        dto.setPoints(a.getPoints());
        return dto;
    }

    private LedgerResponseDTO toLedgerResponse(LoyaltyLedger l) {
        LedgerResponseDTO dto = new LedgerResponseDTO();
        dto.setLedgerId(l.getLedgerId());
        dto.setAccountId(l.getAccountId());
        dto.setPoints(l.getPoints());
        dto.setType(l.getType());
        dto.setCreatedAt(l.getCreatedAt());
        dto.setExpiresAt(l.getExpiresAt());
        return dto;
    }
}