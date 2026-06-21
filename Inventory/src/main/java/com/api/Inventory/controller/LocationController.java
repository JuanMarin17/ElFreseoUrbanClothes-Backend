package com.api.Inventory.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.Inventory.dto.ApiResponseDTO;
import com.api.Inventory.dto.LocationRequestDTO;
import com.api.Inventory.dto.LocationResponseDTO;
import com.api.Inventory.service.LocationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<LocationResponseDTO> createLocation(@RequestBody LocationRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.createLocation(dto));
    }

    @GetMapping
    public ResponseEntity<List<LocationResponseDTO>> getLocations() {
        return ResponseEntity.ok(locationService.getLocationsByStore());
    }

    @PutMapping("/{locationId}")
    public ResponseEntity<LocationResponseDTO> updateLocation(@PathVariable UUID locationId,
                                                                @RequestBody LocationRequestDTO dto) {
        return ResponseEntity.ok(locationService.updateLocation(locationId, dto));
    }

    @DeleteMapping("/{locationId}")
    public ResponseEntity<ApiResponseDTO> deleteLocation(@PathVariable UUID locationId) {
        return ResponseEntity.ok(locationService.deleteLocation(locationId));
    }
}