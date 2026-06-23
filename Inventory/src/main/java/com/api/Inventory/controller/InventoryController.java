package com.api.Inventory.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.Inventory.dto.InventoryBalanceResponseDTO;
import com.api.Inventory.dto.MovementRequestDTO;
import com.api.Inventory.dto.MovementResponseDTO;
import com.api.Inventory.service.InventoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/movements")
    public ResponseEntity<MovementResponseDTO> registerMovement(@RequestBody MovementRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.registerMovement(dto));
    }

    @PostMapping("/movements/batch")
    public ResponseEntity<List<MovementResponseDTO>> registerMovementsBatch(@RequestBody List<MovementRequestDTO> dtos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.registerMovementsBatch(dtos));
    }

    @GetMapping("/balance")
    public ResponseEntity<List<InventoryBalanceResponseDTO>> getBalance() {
        return ResponseEntity.ok(inventoryService.getBalanceByStore());
    }

    @GetMapping("/movements")
    public ResponseEntity<List<MovementResponseDTO>> getMovements() {
        return ResponseEntity.ok(inventoryService.getMovementsByStore());
    }

    @GetMapping("/movements/variant/{variantId}")
    public ResponseEntity<List<MovementResponseDTO>> getMovementsByVariant(@PathVariable UUID variantId) {
        return ResponseEntity.ok(inventoryService.getMovementsByVariant(variantId));
    }
}