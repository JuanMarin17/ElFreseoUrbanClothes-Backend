package com.api.product.controller;

import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.api.product.client.MediaServiceClient;
import com.api.product.dto.CloudinarySignatureResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cloudinary")
@RequiredArgsConstructor
public class CloudinaryController {

    private final MediaServiceClient mediaServiceClient;
    private final Set<String> allowedFolders = Set.of("productos");

    @GetMapping("/signature")
    public ResponseEntity<CloudinarySignatureResponse> getSignature(
            @RequestParam(defaultValue = "productos") String folder) {

        if (!allowedFolders.contains(folder)) {
            throw new RuntimeException("Carpeta no permitida");
        }

        return ResponseEntity.ok(mediaServiceClient.getSignature(folder));
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("image") MultipartFile image) {

        if (image.isEmpty()) {
            throw new RuntimeException("El archivo de imagen está vacío");
        }

        String url = mediaServiceClient.upload(image, "productos");
        return ResponseEntity.ok(Map.of("url", url));
    }
}
