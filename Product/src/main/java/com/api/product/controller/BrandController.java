package com.api.product.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.product.dto.BrandRequestDTO;
import com.api.product.dto.BrandResponseDTO;
import com.api.product.service.BrandService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @GetMapping("/active")
    public ResponseEntity<List<BrandResponseDTO>> getActiveBrands() {
        return ResponseEntity.ok(brandService.listAllActiveBrands());
    }

    @GetMapping("/getAllBrands")
    public ResponseEntity<List<BrandResponseDTO>> getAllBrands() {
        return ResponseEntity.ok(brandService.listAllBrands());
    }

    @PostMapping("/createBrand")
    public ResponseEntity<BrandResponseDTO> createBrand(@RequestBody BrandRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(brandService.createBrand(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BrandResponseDTO> updateBrand(
            @PathVariable UUID id, @RequestBody BrandRequestDTO dto) {
        return brandService.updateBrand(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/inactive/{id}")
    public ResponseEntity<BrandResponseDTO> inactiveBrand(@PathVariable UUID id) {
        return brandService.inactiveBrand(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/active/{id}")
    public ResponseEntity<BrandResponseDTO> activeBrand(@PathVariable UUID id) {
        return brandService.activeBrand(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}