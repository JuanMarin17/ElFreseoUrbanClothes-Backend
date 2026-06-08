package com.api.Cms.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResponseEntity<List<CmsPageResponseDTO>> getMyPages() {
        return ResponseEntity.ok(cmsService.getMyPages());
    }
}