package com.api.Cms.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.Cms.entity.CmsPage;

public interface CmsPageRepository extends JpaRepository<CmsPage, UUID> {
    List<CmsPage> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<CmsPage> findByStoreIdOrderByCreatedAtDesc(UUID storeId);
}