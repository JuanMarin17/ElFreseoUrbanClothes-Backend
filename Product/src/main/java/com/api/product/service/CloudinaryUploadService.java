package com.api.product.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CloudinaryUploadService {

    private final Cloudinary cloudinary;

    public String uploadProductImage(MultipartFile file) {
        try {
            var result = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", "productos"));

            return result.get("secure_url").toString();

        } catch (Exception e) {
            throw new RuntimeException("Error al subir imagen: " + e.getMessage());
        }
    }
}