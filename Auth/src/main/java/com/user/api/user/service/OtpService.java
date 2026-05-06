package com.user.api.user.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.user.api.user.entity.SecretKey;
import com.user.api.user.repository.SecretKeyRepository;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtpService {

    /**
     * Instancia de la Clase GoogleAuthenticator para acceder a la funciones integradas.
     */
    private GoogleAuthenticator gAuth = new GoogleAuthenticator();

    private final SecretKeyRepository secretKeyRepository;

    private final PasswordEncoder passwordEncoder;

    /**
     * Generador de la llave unica del usuario para poder crear codigos unicos al momento de hacer un registro o un inicio de sesión
     * 
     * @return
     */
    public String userSecretKey(){
        GoogleAuthenticatorKey gKey = gAuth.createCredentials();
        return gKey.getKey();
    }

    /**
     * Generador de codigo otp para enviarlo al correo del usuario, recibe la llave unica guardada en la base de datos para generar un codigo unico
     * 
     * @param userSecretKey
     * @return
     */
    public String generateOtp(String userSecretKey){
        Integer code = gAuth.getTotpPassword(userSecretKey);

        Optional<SecretKey> optionalSecret = secretKeyRepository.findBySecretKey(userSecretKey);

        if(optionalSecret.isEmpty()){
            throw new RuntimeException("Clave secreta no encontrada");
        }

        SecretKey secret = optionalSecret.get();

        secret.setCode(passwordEncoder.encode(code.toString()));
        secret.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        secretKeyRepository.save(secret);

        return code.toString();
    }

    /**
     * Metodo para poder verificar el codigo enviado por el usuario tiene un tiempo estimado de 30 segundos para que el codigo sea valido, recibe la llave secreta pasa hacer la validacion
     * 
     * @param userSecretKey
     * @param userOtp
     * @return
     */
    public Boolean validateOtp(String userSecretKey, String userOtp){
        Optional<SecretKey> optionalSecret = secretKeyRepository.findBySecretKey(userSecretKey);

        if(optionalSecret.isEmpty()){
            throw new RuntimeException("Clave secreta no encontrada");
        }

        SecretKey secret = optionalSecret.get();

        Boolean notExpired = LocalDateTime.now().isBefore(secret.getExpiresAt());
        Boolean match = passwordEncoder.matches(userOtp , secret.getCode());

        System.out.println(notExpired + " " + match + " " + userOtp);

        if(notExpired && match){
            secret.setCode(null);
            secret.setExpiresAt(null);
            secretKeyRepository.save(secret);

            return true;
        }

        return false;
    }

}
