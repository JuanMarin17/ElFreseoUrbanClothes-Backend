package com.apiPreferences.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apiPreferences.entity.Preferences;

@Repository
public interface PreferencesRepository extends JpaRepository<Preferences, Long>{
    
}
