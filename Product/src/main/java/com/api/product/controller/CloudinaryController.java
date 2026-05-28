package com.api.product.controller;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.api.product.dto.CloudinarySignatureResponse;
import com.api.product.service.CloudinaryService;
import com.api.product.service.CloudinarySignatureService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cloudinary")
@RequiredArgsConstructor
public class CloudinaryController {

    private final CloudinarySignatureService signatureService;
    private final CloudinaryService cloudinaryService;

    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    private final Set<String> allowedFolders = Set.of("productos");

    /** Genera firma para que el frontend suba directamente a Cloudinary */
    @GetMapping("/signature")
    public ResponseEntity<CloudinarySignatureResponse> getSignature(
            @RequestParam(defaultValue = "productos") String folder) {

        if (!allowedFolders.contains(folder)) {
            throw new RuntimeException("Carpeta no permitida");
        }

        Map<String, Object> signatureData = signatureService.generateSignature(folder);

        CloudinarySignatureResponse response = new CloudinarySignatureResponse(
                signatureData.get("signature").toString(),
                Long.parseLong(signatureData.get("timestamp").toString()),
                apiKey,
                cloudName,
                folder);

        return ResponseEntity.ok(response);
    }

    /**
     * El frontend envía el archivo y el backend se encarga de subirlo a Cloudinary.
     * Devuelve la URL pública de la imagen.
     *
     * POST /api/v1/cloudinary/upload
     * Content-Type: multipart/form-data
     * Body: image (file)
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("image") MultipartFile image) {

        if (image.isEmpty()) {
            throw new RuntimeException("El archivo de imagen está vacío");
        }

        String url = cloudinaryService.upload(image, "productos");
        return ResponseEntity.ok(Map.of("url", url));
    }
}