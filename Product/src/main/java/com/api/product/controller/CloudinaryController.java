package com.api.product.controller;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.api.product.dto.CloudinarySignatureResponse;
import com.api.product.service.CloudinarySignatureService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cloudinary")
@RequiredArgsConstructor
public class CloudinaryController {

    private final CloudinarySignatureService signatureService;

    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    // carpetas permitidas (seguridad)
    private final Set<String> allowedFolders = Set.of("productos");

    @GetMapping("/signature")
    public ResponseEntity<CloudinarySignatureResponse> getSignature(
            @RequestParam(defaultValue = "productos") String folder
    ) {

        if (!allowedFolders.contains(folder)) {
            throw new RuntimeException("Carpeta no permitida");
        }

        Map<String, Object> signatureData = signatureService.generateSignature(folder);

        CloudinarySignatureResponse response = new CloudinarySignatureResponse(
                signatureData.get("signature").toString(),
                Long.parseLong(signatureData.get("timestamp").toString()),
                apiKey,
                cloudName,
                folder
        );

        return ResponseEntity.ok(response);
    }
}