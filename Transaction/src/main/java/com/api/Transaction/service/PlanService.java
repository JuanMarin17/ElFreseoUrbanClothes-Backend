package com.api.Transaction.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.api.Transaction.dto.PlanResponseDTO;
import com.api.Transaction.entity.Plan;
import com.api.Transaction.exception.PlanNotFoundException;
import com.api.Transaction.repository.PlanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    public List<PlanResponseDTO> getAllPlans() {
        return planRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public PlanResponseDTO getPlanById(UUID planId) {
        return toResponse(findOrThrow(planId));
    }

    public Plan findOrThrow(UUID planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException("Plan no encontrado: " + planId));
    }

    private PlanResponseDTO toResponse(Plan plan) {
        PlanResponseDTO dto = new PlanResponseDTO();
        dto.setPlanId(plan.getPlanId());
        dto.setName(plan.getName());
        dto.setPrice(plan.getPrice());
        dto.setMaxProducts(plan.getMaxProducts());
        dto.setMaxPages(plan.getMaxPages());
        dto.setMaxAiCalls(plan.getMaxAiCalls());
        dto.setFeatures(plan.getFeatures());
        return dto;
    }
}