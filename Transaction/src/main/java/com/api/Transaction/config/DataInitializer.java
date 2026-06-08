package com.api.Transaction.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.api.Transaction.entity.Plan;
import com.api.Transaction.enums.PlanName;
import com.api.Transaction.repository.PlanRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Inicializa los 4 planes de Vexio en la BD al arrancar.
 * Solo los crea si no existen (idempotente).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

        private final PlanRepository planRepository;

        @Override
        public void run(String... args) {
                createIfNotExists(PlanName.GRATUITO, BigDecimal.ZERO, 10, 1, 5,
                                "{\"ia\":false,\"customDomain\":false,\"analytics\":false}");
                createIfNotExists(PlanName.BASICO, new BigDecimal("29000"), 100, 5, 50,
                                "{\"ia\":true,\"customDomain\":false,\"analytics\":true}");
                createIfNotExists(PlanName.PRO, new BigDecimal("79000"), 500, 15, 200,
                                "{\"ia\":true,\"customDomain\":true,\"analytics\":true,\"priority\":false}");
                createIfNotExists(PlanName.PREMIUM, new BigDecimal("149000"), null, null, null,
                                "{\"ia\":true,\"customDomain\":true,\"analytics\":true,\"priority\":true,\"unlimited\":true}");
                log.info("Planes de Vexio inicializados correctamente");
        }

        private void createIfNotExists(PlanName name, BigDecimal price,
                        Integer maxProducts, Integer maxPages,
                        Integer maxAiCalls, String features) {
                if (planRepository.findByName(name).isPresent())
                        return;

                Plan plan = new Plan();
                plan.setName(name);
                plan.setPrice(price);
                plan.setMaxProducts(maxProducts);
                plan.setMaxPages(maxPages);
                plan.setMaxAiCalls(maxAiCalls);
                plan.setFeatures(features);
                planRepository.save(plan);
                log.info("Plan {} creado", name);
        }
}
