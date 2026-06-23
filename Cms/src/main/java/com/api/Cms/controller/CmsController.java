package com.api.Cms.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.Cms.dto.CmsGenerateRequestDTO;
import com.api.Cms.dto.CmsPageResponseDTO;
import com.api.Cms.service.CmsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cms")
@RequiredArgsConstructor
public class CmsController {

    private final CmsService cmsService;

    @PostMapping("/generate")
    public ResponseEntity<CmsPageResponseDTO> generatePage(
            @RequestBody(required = false) CmsGenerateRequestDTO dto) {
        if (dto == null)
            dto = new CmsGenerateRequestDTO();
        return ResponseEntity.status(HttpStatus.CREATED).body(cmsService.generatePage(dto));
    }

    @GetMapping("/my-pages")
    public ResponseEntity<Page<CmsPageResponseDTO>> getMyPages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(cmsService.getMyPages(pageable));
    }
}