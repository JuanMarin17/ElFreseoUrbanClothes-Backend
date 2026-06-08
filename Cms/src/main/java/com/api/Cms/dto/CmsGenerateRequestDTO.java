package com.api.Cms.dto;

import lombok.Data;

@Data
public class CmsGenerateRequestDTO {
    private String query; // opcional: el usuario puede pedir algo específico
}