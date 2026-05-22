package com.api.Returns.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.Returns.dto.ApiResponseDTO;
import com.api.Returns.dto.ReturnRequestDTO;
import com.api.Returns.dto.ReturnResponseDTO;
import com.api.Returns.dto.UpdateReturnStatusDTO;
import com.api.Returns.service.ReturnService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/returns")
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnService returnService;

    @PostMapping("/createReturn")
    public ResponseEntity<ReturnResponseDTO> createReturn(@RequestBody ReturnRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(returnService.createReturn(dto));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ReturnResponseDTO>> getMyReturns() {
        return ResponseEntity.ok(returnService.getMyReturns());
    }

    @GetMapping("/getReturnsByStore")
    public ResponseEntity<List<ReturnResponseDTO>> getReturnsByStore() {
        return ResponseEntity.ok(returnService.getReturnsByStore());
    }

    @GetMapping("/{returnId}")
    public ResponseEntity<ReturnResponseDTO> getReturnById(@PathVariable UUID returnId) {
        return ResponseEntity.ok(returnService.getReturnById(returnId));
    }

    @PatchMapping("/{returnId}/status")
    public ResponseEntity<ReturnResponseDTO> updateStatus(
            @PathVariable UUID returnId,
            @RequestBody UpdateReturnStatusDTO dto) {
        return ResponseEntity.ok(returnService.updateReturnStatus(returnId, dto));
    }

    @DeleteMapping("/{returnId}")
    public ResponseEntity<ApiResponseDTO> cancelReturn(@PathVariable UUID returnId) {
        return ResponseEntity.ok(returnService.cancelReturn(returnId));
    }
}