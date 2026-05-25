package com.api.LoyalCustomer.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.LoyalCustomer.dto.ApiResponseDTO;
import com.api.LoyalCustomer.dto.LedgerResponseDTO;
import com.api.LoyalCustomer.dto.LoyaltyAccountResponseDTO;
import com.api.LoyalCustomer.dto.RedeemPointsRequestDTO;
import com.api.LoyalCustomer.service.LoyaltyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    @GetMapping("/me")
    public ResponseEntity<LoyaltyAccountResponseDTO> getMyAccount() {
        return ResponseEntity.ok(loyaltyService.getMyAccount());
    }

    @GetMapping("/me/ledger")
    public ResponseEntity<List<LedgerResponseDTO>> getMyLedger() {
        return ResponseEntity.ok(loyaltyService.getMyLedger());
    }

    @PostMapping("/redeem")
    public ResponseEntity<ApiResponseDTO> redeemPoints(@RequestBody RedeemPointsRequestDTO dto) {
        return ResponseEntity.ok(loyaltyService.redeemPoints(dto));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<LoyaltyAccountResponseDTO> getAccountByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(loyaltyService.getAccountByUser(userId));
    }
}