package com.api.Store.repository;

import com.api.Store.entity.StoreCmsAbout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoreCmsAboutRepository extends JpaRepository<StoreCmsAbout, UUID> {
}
