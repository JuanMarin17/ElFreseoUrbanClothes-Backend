package com.api.Cms.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.api.Cms.entity.CmsPage;

public interface CmsPageRepository extends JpaRepository<CmsPage, UUID> {
    Page<CmsPage> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<CmsPage> findByStoreIdOrderByCreatedAtDesc(UUID storeId);
}