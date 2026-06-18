package com.api.Transaction.service;

import com.api.Transaction.dto.AdminPlanStatsDTO;
import com.api.Transaction.dto.MonthlyRevenueDTO;
import com.api.Transaction.entity.Plan;
import com.api.Transaction.entity.PlanChangeHistory;
import com.api.Transaction.entity.StoreSubscription;
import com.api.Transaction.enums.PlanChangeReason;
import com.api.Transaction.enums.PlanName;
import com.api.Transaction.enums.SubscriptionStatus;
import com.api.Transaction.exception.UnauthorizedException;
import com.api.Transaction.repository.PlanChangeHistoryRepository;
import com.api.Transaction.repository.PlanRepository;
import com.api.Transaction.repository.StoreSubscriptionRepository;
import com.common_request_context_starter.context.RequestContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final PlanRepository planRepository;
    private final StoreSubscriptionRepository subscriptionRepository;
    private final PlanChangeHistoryRepository historyRepository;

    private static final String[] MONTHS_ES = {
        "Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"
    };

    // ── Estadísticas por plan ─────────────────────────────────────────────────

    public List<AdminPlanStatsDTO> getPlanStats() {
        validateSuperAdmin();
        return computePlanStats();
    }

    private List<AdminPlanStatsDTO> computePlanStats() {
        List<Plan> plans = planRepository.findAll();
        List<StoreSubscription> allSubs = subscriptionRepository.findAllWithPlan();
        List<PlanChangeHistory> allHistory = historyRepository.findAllWithPlans();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfThisMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);

        List<AdminPlanStatsDTO> result = new ArrayList<>();

        for (Plan plan : plans) {
            PlanName planName = plan.getName();

            long activeStores = allSubs.stream()
                    .filter(s -> s.getPlan().getName() == planName
                            && s.getStatus() == SubscriptionStatus.ACTIVE)
                    .count();

            long newThisMonth = allSubs.stream()
                    .filter(s -> s.getPlan().getName() == planName
                            && s.getStartedAt() != null
                            && !s.getStartedAt().isBefore(startOfThisMonth))
                    .count();

            long newLastMonth = allSubs.stream()
                    .filter(s -> s.getPlan().getName() == planName
                            && s.getStartedAt() != null
                            && !s.getStartedAt().isBefore(startOfLastMonth)
                            && s.getStartedAt().isBefore(startOfThisMonth))
                    .count();

            double growthPercent = newLastMonth > 0
                    ? round1((double) (newThisMonth - newLastMonth) / newLastMonth * 100)
                    : (newThisMonth > 0 ? 100.0 : 0.0);

            long cancelledThisMonth = allHistory.stream()
                    .filter(h -> h.getFromPlan() != null
                            && h.getFromPlan().getName() == planName
                            && h.getReason() == PlanChangeReason.CANCELLED
                            && h.getChangedAt() != null
                            && !h.getChangedAt().isBefore(startOfThisMonth))
                    .count();

            // Tiendas que tenían este plan al iniciar el mes actual
            long totalAtStartOfMonth = allSubs.stream()
                    .filter(s -> s.getPlan().getName() == planName
                            && s.getStartedAt() != null
                            && s.getStartedAt().isBefore(startOfThisMonth)
                            && (s.getStatus() == SubscriptionStatus.ACTIVE
                                || (s.getExpiresAt() != null
                                    && !s.getExpiresAt().isBefore(startOfThisMonth))))
                    .count();

            double churnPercent = totalAtStartOfMonth > 0
                    ? round1((double) cancelledThisMonth / totalAtStartOfMonth * 100)
                    : 0.0;

            double mrr = plan.getPrice().multiply(BigDecimal.valueOf(activeStores))
                    .setScale(2, RoundingMode.HALF_UP).doubleValue();

            result.add(AdminPlanStatsDTO.builder()
                    .id(planName.name().toLowerCase())
                    .label(toLabel(planName))
                    .price(plan.getPrice())
                    .stores(activeStores)
                    .mrr(mrr)
                    .growthPercent(growthPercent)
                    .churnPercent(churnPercent)
                    .newThisMonth(newThisMonth)
                    .build());
        }

        return result;
    }

    // ── Ingresos mensuales ────────────────────────────────────────────────────

    public List<MonthlyRevenueDTO> getMonthlyRevenue(int months) {
        validateSuperAdmin();

        List<Plan> plans = planRepository.findAll();
        List<StoreSubscription> allSubs = subscriptionRepository.findAllWithPlan();

        LocalDateTime now = LocalDateTime.now();
        List<MonthlyRevenueDTO> result = new ArrayList<>();

        for (int i = months - 1; i >= 0; i--) {
            LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).toLocalDate().atStartOfDay();
            LocalDateTime monthEnd = monthStart.plusMonths(1);
            String monthName = MONTHS_ES[monthStart.getMonthValue() - 1];

            double basico = 0, pro = 0, premium = 0;

            for (Plan plan : plans) {
                if (plan.getName() == PlanName.GRATUITO) continue;

                long count = allSubs.stream()
                        .filter(s -> s.getPlan().getName() == plan.getName()
                                && s.getStartedAt() != null
                                && !s.getStartedAt().isAfter(monthEnd)
                                && (s.getStatus() == SubscriptionStatus.ACTIVE
                                    || (s.getExpiresAt() != null
                                        && !s.getExpiresAt().isBefore(monthStart))))
                        .count();

                double revenue = round2(plan.getPrice().doubleValue() * count);
                switch (plan.getName()) {
                    case BASICO -> basico = revenue;
                    case PRO -> pro = revenue;
                    case PREMIUM -> premium = revenue;
                    default -> {}
                }
            }

            result.add(MonthlyRevenueDTO.builder()
                    .month(monthName)
                    .basico(basico)
                    .pro(pro)
                    .premium(premium)
                    .build());
        }

        return result;
    }

    // ── Export CSV ────────────────────────────────────────────────────────────

    public String buildCsv() {
        validateSuperAdmin();
        List<AdminPlanStatsDTO> stats = computePlanStats();

        StringBuilder sb = new StringBuilder();
        sb.append("Plan,Precio/mo,Tiendas,MRR,Crecimiento%,Churn%,Nuevas este mes,ARR Proyectado\n");

        for (AdminPlanStatsDTO s : stats) {
            double precio = s.getPrice() != null ? s.getPrice().doubleValue() : 0.0;
            double arr = round2(s.getMrr() * 12);
            sb.append(String.format("%s,%.2f,%d,%.2f,%.1f,%.1f,%d,%.2f\n",
                    s.getLabel(), precio, s.getStores(), s.getMrr(),
                    s.getGrowthPercent(), s.getChurnPercent(), s.getNewThisMonth(), arr));
        }

        return sb.toString();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void validateSuperAdmin() {
        if (!"SUPERADMIN".equals(RequestContext.getHeader("X-User-Role")))
            throw new UnauthorizedException("Solo el SUPERADMIN puede acceder a este reporte");
    }

    private static String toLabel(PlanName name) {
        return switch (name) {
            case GRATUITO -> "Gratuito";
            case BASICO -> "Básico";
            case PRO -> "Pro";
            case PREMIUM -> "Premium";
        };
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
