package com.api.Preferences.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.Preferences.dto.UserBehaviorRequestDTO;
import com.api.Preferences.dto.UserBehaviorResponseDTO;
import com.api.Preferences.dto.UserPreferenceRequestDTO;
import com.api.Preferences.dto.UserPreferenceResponseDTO;
import com.api.Preferences.service.PreferenceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/preferences")
@RequiredArgsConstructor
public class PreferenceController {

    private final PreferenceService preferenceService;

    @GetMapping("/getMyPreferences")
    public ResponseEntity<List<UserPreferenceResponseDTO>> getMyPreferences() {
        return ResponseEntity.ok(preferenceService.getMyPreferences());
    }

    @PostMapping("/savePreference")
    public ResponseEntity<UserPreferenceResponseDTO> savePreference(
            @RequestBody UserPreferenceRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(preferenceService.savePreference(dto));
    }

    @PostMapping("/behavior")
    public ResponseEntity<UserBehaviorResponseDTO> trackBehavior(
            @RequestBody UserBehaviorRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(preferenceService.trackBehavior(dto));
    }

    @GetMapping("/behavior")
    public ResponseEntity<List<UserBehaviorResponseDTO>> getMyBehaviors() {
        return ResponseEntity.ok(preferenceService.getMyBehaviors());
    }
}