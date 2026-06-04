package com.api.Transaction.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.Transaction.dto.PlanResponseDTO;
import com.api.Transaction.service.PlanService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/transactions/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    /** Lista todos los planes disponibles (público). */
    @GetMapping
    public ResponseEntity<List<PlanResponseDTO>> getAll() {
        return ResponseEntity.ok(planService.getAllPlans());
    }

    /** Obtiene un plan por ID. */
    @GetMapping("/{planId}")
    public ResponseEntity<PlanResponseDTO> getById(@PathVariable UUID planId) {
        return ResponseEntity.ok(planService.getPlanById(planId));
    }
}
