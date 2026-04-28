package com.apiPreferences.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.apiPreferences.dto.PreferencesRequestDTO;
import com.apiPreferences.dto.PreferencesResponseDTO;
import com.apiPreferences.entity.Preferences;
import com.apiPreferences.repository.PreferencesRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PreferencesService {
    private final PreferencesRepository preferencesRepository;

    public PreferencesResponseDTO createPrefence(PreferencesRequestDTO preferencesRequestDTO) {
        Preferences preference = new Preferences();
        preference.setUser_id(preferencesRequestDTO.getUser_id());
        preference.setSports(preferencesRequestDTO.getSports());
        preference.setColors(preferencesRequestDTO.getColors());
        preference.setPreferences_size(preferencesRequestDTO.getPreferences_size());
        preference.setClothing_style(preferencesRequestDTO.getClothing_style());

        preferencesRepository.save(preference);

        PreferencesResponseDTO response = new PreferencesResponseDTO();
        response.setUser_id(preference.getUser_id());
        response.setSports(preference.getSports());
        response.setColors(preference.getColors());
        response.setPreferences_size(preference.getPreferences_size());
        response.setClothing_style(preference.getClothing_style());

        return response;
    }
    
    public List<PreferencesResponseDTO> preferenceList() {
        List<Preferences> preferences = preferencesRepository.findAll();
        List<PreferencesResponseDTO> listpreference = new ArrayList<>();

        for (Preferences preference : preferences) {
            PreferencesResponseDTO responseDTO = new PreferencesResponseDTO();
            responseDTO.setUser_id(preference.getUser_id());
            responseDTO.setSports(preference.getSports());
            responseDTO.setColors(preference.getColors());
            responseDTO.setPreferences_size(preference.getPreferences_size());
            responseDTO.setClothing_style(preference.getClothing_style());
            listpreference.add(responseDTO);
        }
        return listpreference;
    }

    public Optional<PreferencesResponseDTO> listId(Long id) {
        Optional<Preferences> optionalPreference = preferencesRepository.findById(id);

        if (optionalPreference.isPresent()) {
            Preferences preferences = optionalPreference.get();
            PreferencesResponseDTO responseDTO = new PreferencesResponseDTO();
            responseDTO.setUser_id(preferences.getUser_id());
            responseDTO.setSports(preferences.getSports());
            responseDTO.setColors(preferences.getColors());
            responseDTO.setPreferences_size(preferences.getPreferences_size());
            responseDTO.setClothing_style(preferences.getClothing_style());
            return Optional.of(responseDTO);
        } else {
            return Optional.empty();
        }
    }

    public PreferencesResponseDTO updatePreference(Long id, PreferencesRequestDTO requestDTO) {
        Optional<Preferences> optionalPreferences = preferencesRepository.findById(id);
        
        if (optionalPreferences.isPresent()) {
            Preferences preferences = optionalPreferences.get();

            preferences.setUser_id(requestDTO.getUser_id());
            preferences.setSports(requestDTO.getSports());

            
        }
        

    }
}
