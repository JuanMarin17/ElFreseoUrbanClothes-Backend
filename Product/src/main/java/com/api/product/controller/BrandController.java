package com.api.product.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.api.product.dto.BrandRequestDTO;
import com.api.product.dto.BrandResponseDTO;
import com.api.product.service.BrandService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    /** GET /api/brands/active */
    @GetMapping("/active")
    public ResponseEntity<List<BrandResponseDTO>> getActiveBrands() {
        return ResponseEntity.ok(brandService.listAllActiveBrands());
    }

    @PostMapping
    public ResponseEntity<BrandResponseDTO> createBrand(@Validated @RequestBody BrandRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(brandService.createBrand(dto));
    }
}