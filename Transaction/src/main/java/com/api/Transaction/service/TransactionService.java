package com.api.Transaction.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.Transaction.dto.CheckoutRequestDTO;
import com.api.Transaction.dto.CheckoutResponseDTO;
import com.api.Transaction.dto.PlanChangeHistoryResponseDTO;
import com.api.Transaction.dto.StoreLimitsResponseDTO;
import com.api.Transaction.dto.SubscriptionResponseDTO;
import com.api.Transaction.dto.TransactionResponseDTO;
import com.api.Transaction.entity.Plan;
import com.api.Transaction.entity.PlanChangeHistory;
import com.api.Transaction.entity.StoreSubscription;
import com.api.Transaction.entity.Transaction;
import com.api.Transaction.enums.PlanChangeReason;
import com.api.Transaction.enums.PlanName;
import com.api.Transaction.enums.SubscriptionStatus;
import com.api.Transaction.enums.TransactionStatus;
import com.api.Transaction.enums.TransactionType;
import com.api.Transaction.exception.PlanNotFoundException;
import com.api.Transaction.exception.SubscriptionNotFoundException;
import com.api.Transaction.exception.UnauthorizedException;
import com.api.Transaction.repository.PlanChangeHistoryRepository;
import com.api.Transaction.repository.PlanRepository;
import com.api.Transaction.repository.StoreSubscriptionRepository;
import com.api.Transaction.repository.TransactionRepository;
import com.common_request_context_starter.context.RequestContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final PlanRepository planRepository;
    private final StoreSubscriptionRepository subscriptionRepository;
    private final TransactionRepository transactionRepository;
    private final PlanChangeHistoryRepository historyRepository;
    private final MercadoPagoService mercadoPagoService;

    @Transactional
    public CheckoutResponseDTO checkout(CheckoutRequestDTO dto) {
        validateOwner();
        Plan plan = planRepository.findByName(dto.getPlanName())
                .orElseThrow(() -> new PlanNotFoundException("Plan no encontrado: " + dto.getPlanName()));
        StoreSubscription current = subscriptionRepository
                .findByStoreIdAndStatus(dto.getStoreId(), SubscriptionStatus.ACTIVE).orElse(null);

        Transaction tx = new Transaction();
        tx.setStoreId(dto.getStoreId());
        tx.setPlan(plan);
        tx.setAmount(plan.getPrice());
        tx.setStatus(TransactionStatus.PENDING);
        tx.setCreatedAt(LocalDateTime.now());
        tx.setType(resolveType(current, dto.getPlanName()));
        Transaction saved = transactionRepository.save(tx);

        if (dto.getPlanName() == PlanName.GRATUITO) {
            saved.setStatus(TransactionStatus.APPROVED);
            transactionRepository.save(saved);
            activateSubscription(dto.getStoreId(), plan, current, saved.getType());
            CheckoutResponseDTO res = new CheckoutResponseDTO();
            res.setTransactionId(saved.getTransactionId());
            res.setStatus("APPROVED");
            return res;
        }

        String paymentUrl = mercadoPagoService.createPaymentPreference(
                plan.getName().name(), plan.getPrice(), saved.getTransactionId().toString());
        CheckoutResponseDTO res = new CheckoutResponseDTO();
        res.setTransactionId(saved.getTransactionId());
        res.setPaymentUrl(paymentUrl);
        res.setStatus("PENDING");
        return res;
    }

    @Transactional
    public void handleWebhook(String externalRef, String mpPaymentId, String mpStatus) {
        UUID transactionId;
        try {
            transactionId = UUID.fromString(externalRef);
        } catch (Exception e) {
            return;
        }
        Transaction tx = transactionRepository.findById(transactionId).orElse(null);
        if (tx == null)
            return;
        tx.setMpPaymentId(mpPaymentId);
        switch (mpStatus) {
            case "approved" -> {
                tx.setStatus(TransactionStatus.APPROVED);
                transactionRepository.save(tx);
                StoreSubscription cur = subscriptionRepository
                        .findByStoreIdAndStatus(tx.getStoreId(), SubscriptionStatus.ACTIVE).orElse(null);
                activateSubscription(tx.getStoreId(), tx.getPlan(), cur, tx.getType());
            }
            case "rejected" -> {
                tx.setStatus(TransactionStatus.REJECTED);
                transactionRepository.save(tx);
            }
        }
    }

    private void activateSubscription(UUID storeId, Plan plan, StoreSubscription current, TransactionType type) {
        if (current != null) {
            current.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(current);
            PlanChangeHistory h = new PlanChangeHistory();
            h.setStoreId(storeId);
            h.setFromPlan(current.getPlan());
            h.setToPlan(plan);
            h.setChangedAt(LocalDateTime.now());
            h.setReason(switch (type) {
                case UPGRADE -> PlanChangeReason.UPGRADE;
                case DOWNGRADE -> PlanChangeReason.DOWNGRADE;
                default -> PlanChangeReason.RENEWAL;
            });
            historyRepository.save(h);
        }
        StoreSubscription sub = new StoreSubscription();
        sub.setStoreId(storeId);
        sub.setPlan(plan);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setStartedAt(LocalDateTime.now());
        sub.setExpiresAt(LocalDateTime.now().plusDays(30));
        sub.setRenewalAt(LocalDateTime.now().plusDays(27));
        subscriptionRepository.save(sub);
    }

    public SubscriptionResponseDTO getActiveSubscription(UUID storeId) {
        return toSubResponse(subscriptionRepository.findByStoreIdAndStatus(storeId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new SubscriptionNotFoundException("No hay suscripción activa para esta tienda")));
    }

    public StoreLimitsResponseDTO getStoreLimits(UUID storeId) {
        StoreSubscription sub = subscriptionRepository.findByStoreIdAndStatus(storeId, SubscriptionStatus.ACTIVE)
                .orElse(null);
        StoreLimitsResponseDTO dto = new StoreLimitsResponseDTO();
        if (sub == null) {
            dto.setPlanName(PlanName.GRATUITO);
            dto.setActive(false);
            dto.setMaxProducts(10);
            dto.setMaxPages(1);
            dto.setMaxAiCalls(5);
            dto.setFeatures("{}");
            return dto;
        }
        Plan p = sub.getPlan();
        dto.setPlanName(p.getName());
        dto.setActive(true);
        dto.setMaxProducts(p.getMaxProducts());
        dto.setMaxPages(p.getMaxPages());
        dto.setMaxAiCalls(p.getMaxAiCalls());
        dto.setFeatures(p.getFeatures());
        return dto;
    }

    public List<TransactionResponseDTO> getTransactionsByStore(UUID storeId) {
        return transactionRepository.findByStoreIdOrderByCreatedAtDesc(storeId).stream().map(this::toTxResponse)
                .toList();
    }

    public List<PlanChangeHistoryResponseDTO> getPlanHistory(UUID storeId) {
        return historyRepository.findByStoreIdOrderByChangedAtDesc(storeId).stream().map(this::toHistoryResponse)
                .toList();
    }

    public List<TransactionResponseDTO> getAllTransactions() {
        validateSuperAdmin();
        return transactionRepository.findAll().stream().map(this::toTxResponse).toList();
    }

    @Transactional
    public void cancelSubscription(UUID storeId) {
        validateOwner();
        StoreSubscription sub = subscriptionRepository.findByStoreIdAndStatus(storeId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new SubscriptionNotFoundException("No hay suscripción activa para cancelar"));
        sub.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(sub);
        PlanChangeHistory h = new PlanChangeHistory();
        h.setStoreId(storeId);
        h.setFromPlan(sub.getPlan());
        h.setToPlan(sub.getPlan());
        h.setChangedAt(LocalDateTime.now());
        h.setReason(PlanChangeReason.CANCELLED);
        historyRepository.save(h);
    }

    private TransactionType resolveType(StoreSubscription current, PlanName newPlan) {
        if (current == null)
            return TransactionType.NEW;
        int cur = current.getPlan().getName().ordinal(), nw = newPlan.ordinal();
        return nw > cur ? TransactionType.UPGRADE : nw < cur ? TransactionType.DOWNGRADE : TransactionType.RENEWAL;
    }

    private void validateOwner() {
        if (!"OWNER".equals(RequestContext.getHeader("X-User-Role")))
            throw new UnauthorizedException("Solo el OWNER puede realizar esta acción");
    }

    private void validateSuperAdmin() {
        if (!"SUPERADMIN".equals(RequestContext.getHeader("X-User-Role")))
            throw new UnauthorizedException("Solo el SUPERADMIN puede realizar esta acción");
    }

    private SubscriptionResponseDTO toSubResponse(StoreSubscription s) {
        SubscriptionResponseDTO dto = new SubscriptionResponseDTO();
        dto.setSubscriptionId(s.getSubscriptionId());
        dto.setStoreId(s.getStoreId());
        dto.setPlanName(s.getPlan().getName().name());
        dto.setStatus(s.getStatus());
        dto.setStartedAt(s.getStartedAt());
        dto.setExpiresAt(s.getExpiresAt());
        dto.setRenewalAt(s.getRenewalAt());
        return dto;
    }

    private TransactionResponseDTO toTxResponse(Transaction t) {
        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setTransactionId(t.getTransactionId());
        dto.setStoreId(t.getStoreId());
        dto.setPlanName(t.getPlan().getName().name());
        dto.setMpPaymentId(t.getMpPaymentId());
        dto.setAmount(t.getAmount());
        dto.setStatus(t.getStatus());
        dto.setType(t.getType());
        dto.setCreatedAt(t.getCreatedAt());
        return dto;
    }

    private PlanChangeHistoryResponseDTO toHistoryResponse(PlanChangeHistory h) {
        PlanChangeHistoryResponseDTO dto = new PlanChangeHistoryResponseDTO();
        dto.setHistoryId(h.getHistoryId());
        dto.setStoreId(h.getStoreId());
        dto.setFromPlan(h.getFromPlan() != null ? h.getFromPlan().getName().name() : null);
        dto.setToPlan(h.getToPlan().getName().name());
        dto.setChangedAt(h.getChangedAt());
        dto.setReason(h.getReason());
        return dto;
    }
}
