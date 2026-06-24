package com.user.api.user.service;

import java.security.GeneralSecurityException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.user.api.user.exception.GoogleAuthException;

import lombok.extern.slf4j.Slf4j;

/** Verifica idTokens de Google Sign-In contra los servidores de Google. */
@Slf4j
@Service
public class GoogleTokenVerifierService {

    @Value("${google.client-id}")
    private String clientId;

    private GoogleIdTokenVerifier verifier;

    private GoogleIdTokenVerifier getVerifier() {
        if (verifier == null) {
            verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(clientId))
                    .build();
        }
        return verifier;
    }

    public GooglePayload verify(String idTokenString) {
        try {
            GoogleIdToken idToken = getVerifier().verify(idTokenString);
            if (idToken == null) {
                throw new GoogleAuthException("El idToken de Google es inválido o expiró");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            return new GooglePayload(email, name, picture, emailVerified);
        } catch (GeneralSecurityException | java.io.IOException e) {
            log.warn("No se pudo verificar el idToken de Google: {}", e.getMessage());
            throw new GoogleAuthException("No se pudo verificar el idToken de Google");
        } catch (IllegalArgumentException e) {
            throw new GoogleAuthException("El idToken de Google es inválido o expiró");
        }
    }

    public record GooglePayload(String email, String name, String picture, boolean emailVerified) {
    }
}
