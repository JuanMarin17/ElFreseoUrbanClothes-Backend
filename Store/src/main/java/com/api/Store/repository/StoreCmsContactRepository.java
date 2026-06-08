package com.api.Store.repository;

import com.api.Store.entity.StoreCmsContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoreCmsContactRepository extends JpaRepository<StoreCmsContact, UUID> {
}
