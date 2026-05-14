package com.api.product.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CloudinarySignatureService {

    @Value("${cloudinary.api_secret}")
    private String apiSecret;

public Map<String, Object> generateSignature(String folder) {

    long timestamp = System.currentTimeMillis() / 1000;

    String stringToSign = "folder=" + folder + "&timestamp=" + timestamp + apiSecret;

    String signature = sha1Hex(stringToSign);

    return Map.of(
            "signature", signature,
            "timestamp", timestamp,
            "folder", folder
    );
}

    private String sha1Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (Exception e) {
            throw new RuntimeException("Error generando firma SHA1: " + e.getMessage());
        }
    }
}