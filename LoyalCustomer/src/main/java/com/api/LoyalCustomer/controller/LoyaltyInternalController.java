package com.api.LoyalCustomer.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.LoyalCustomer.dto.EarnPointsRequestDTO;
import com.api.LoyalCustomer.dto.LoyaltyAccountResponseDTO;
import com.api.LoyalCustomer.service.LoyaltyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/loyalty/internal")
@RequiredArgsConstructor
public class LoyaltyInternalController {

    private final LoyaltyService loyaltyService;

    @PostMapping("/earn")
    public ResponseEntity<LoyaltyAccountResponseDTO> earnPoints(
            @RequestBody EarnPointsRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(loyaltyService.earnPoints(dto));
    }
}