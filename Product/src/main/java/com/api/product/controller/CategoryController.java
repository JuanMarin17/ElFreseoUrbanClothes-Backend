package com.api.product.controller;

import com.api.product.dto.CategoryRequestDTO;
import com.api.product.dto.CategoryResponseDTO;
import com.api.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/active")
    public ResponseEntity<List<CategoryResponseDTO>> getActiveCategories() {
        return ResponseEntity.ok(categoryService.listAllActiveCategories());
    }

    @GetMapping("/getAllCategories")
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.listAllCategories());
    }

    @PostMapping("/createCategory")
    public ResponseEntity<CategoryResponseDTO> createCategory(@RequestBody CategoryRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable UUID id, @RequestBody CategoryRequestDTO dto) {
        return categoryService.updateCategory(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateCategory(@PathVariable UUID id) {
        categoryService.inactiveCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/active/{id}")
    public ResponseEntity<CategoryResponseDTO> activateCategory(@PathVariable UUID id) {
        return categoryService.activeCategory(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}