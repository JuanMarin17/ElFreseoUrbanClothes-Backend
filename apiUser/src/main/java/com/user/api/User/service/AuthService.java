package com.user.api.user.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.user.api.user.dto.JwtResponseDTO;
import com.user.api.user.dto.LoginRequestDTO;
import com.user.api.user.dto.MessageResponseDTO;
import com.user.api.user.dto.UserRequestDTO;
import com.user.api.user.dto.ValidationCodeDTO;
import com.user.api.user.entity.User;
import com.user.api.user.entity.Role;
import com.user.api.user.entity.SecretKey;
import com.user.api.user.repository.UserRepository;
import com.user.api.user.repository.RoleRepository;
import com.user.api.user.repository.SecretKeyRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final SecretKeyRepository secretKeyRepository;

    @Value("${spring.mail.username}")
    String emailShop;

    private final JwtService jwtService;

    private final PasswordEncoder passwordEncoder;

    private final JavaMailSender mailSender;

    private final OtpService otpService;

    private final RoleRepository roleRepository;

    /**
     * Este es el generador de email, crea un email y lo envia al correo del usuario
     * con el codigo de verificación.
     * 
     * @param to
     * @param code
     */
    public void sendEmail(String to, Integer code) {
        SimpleMailMessage email = new SimpleMailMessage();

        email.setFrom(emailShop);
        email.setTo(to);
        email.setSubject("Codigo de verificación");
        email.setText("Tu codigo de verificación es: " + code);

        mailSender.send(email);
    }

    /**
     * Metodo para poder buscar usuario por medio del correo (Estoy pensando en
     * quitarlo)
     * 
     * @param email
     * @return
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Boolean validateCode(ValidationCodeDTO validationCodeDTO) {

        if (validationCodeDTO.getEmail() == null || validationCodeDTO.getCode() == null) {
            throw new RuntimeException("Ningún campo debe estar vacío");
        }

        Optional<User> optionalUser = findByEmail(validationCodeDTO.getEmail());

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("Usuario con este correo inexistente");
        }

        User user = optionalUser.get();

        SecretKey secretKey = user.getSecretKey();

        Boolean isValid = otpService.validateOtp(secretKey.getSecretKey(), validationCodeDTO.getCode());

        if (!isValid) {
            throw new RuntimeException("El codigo ingresado expiro o es incorrecto");
        }
        
        return true;
    }

    public MessageResponseDTO resendVerificationCode(String email){
        if(email == null){
            throw new RuntimeException("El correo no puede estar vacio");
        }

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario con ese correo inexistente"));

        SecretKey secretKey = secretKeyRepository.findByUser(user).orElseThrow(()-> new RuntimeException("Este usuario no tiene una llave de configuración"));

        Integer newCode = otpService.generateOtp(secretKey.getSecretKey());
        secretKey.setCode(newCode);
        secretKey.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        secretKeyRepository.save(secretKey);

        sendEmail(email, newCode);

        MessageResponseDTO response = new MessageResponseDTO();

        response.setMessage("Se envió un nuevo código a: " + email);

        return response;
    }

    /**
     * Registro de usuario, guarda la informacion inicial del usuario como correo,
     * contraseña, telefono y nombre de usuario
     * 
     * @param userRequestDTO
     * @return
     */
    @Transactional
    public MessageResponseDTO register(UserRequestDTO userRequestDTO) {
        User user = new User();
        MessageResponseDTO messageResponseDTO = new MessageResponseDTO();

        if (userRequestDTO.getUserName() == null ||
                userRequestDTO.getEmail() == null ||
                userRequestDTO.getPassword() == null ||
                userRequestDTO.getPhone() == null) {
            messageResponseDTO.setMessage("Todos los campos deben estar llenos");
            return messageResponseDTO;
        }

        Optional<User> userOptional = findByEmail(userRequestDTO.getEmail());

        if (userOptional.isPresent()) {
            throw new RuntimeException("Este usuario ya existe");
        }

        Optional<User> getUserByName = userRepository.findByUserName(userRequestDTO.getUserName());

        if (getUserByName.isPresent()) {
            throw new RuntimeException("Nombre de usuario ya existente");
        }

        Optional<Role> optionalRole = roleRepository.findByName("USER");

        if (optionalRole.isEmpty()) {
            throw new RuntimeException("Tipo de usuario inexistente");
        }

        Role role = optionalRole.get();

        user.setUserName(userRequestDTO.getUserName());
        user.setEmail(userRequestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
        user.setCreateAt(LocalDateTime.now());
        user.setPhone(userRequestDTO.getPhone());
        user.setRoles(Set.of(role));

        User userSave = userRepository.save(user);

        String userSecretKey = otpService.userSecretKey();

        SecretKey secretKey = new SecretKey();
        secretKey.setSecretKey(userSecretKey);
        secretKey.setUser(userSave);

        secretKeyRepository.save(secretKey);

        Integer code = otpService.generateOtp(userSecretKey);

        sendEmail(userRequestDTO.getEmail(), code);

        messageResponseDTO
                .setMessage("Se envio un correo a " + userRequestDTO.getEmail() + " con el codigo de verificación");

        return messageResponseDTO;
    }

    /**
     * Este es el segundo paso del registro donde el usuario ingresa el codigo que
     * se le envio al usuario y se valida para saber si es correcto o no, luego de
     * la verificacion el usuario se activa para que ya su cuenta quede totalmente
     * funcional.
     * 
     * Tambien genera un jwt para que permita al usuario ingresar directamente sin
     * necesidad de hacer el inicio de sesión
     * 
     * @param secretKeyDTO
     * @returnñ
     */
    @Transactional
    public JwtResponseDTO registerSecondStep(ValidationCodeDTO validationCodeDTO) {

        JwtResponseDTO responseDTO = new JwtResponseDTO();

        if (validationCodeDTO.getEmail() == null || validationCodeDTO.getCode() == null) {
            throw new RuntimeException("Ningún campo puede estar vacio");
        }

        validateCode(validationCodeDTO);

        User user = userRepository.findByEmail(validationCodeDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setIsActive(true);
        userRepository.save(user);

        secretKeyRepository.delete(user.getSecretKey());

        SimpleMailMessage sendEmail = new SimpleMailMessage();

        sendEmail.setFrom(emailShop);
        sendEmail.setTo(validationCodeDTO.getEmail());
        sendEmail.setSubject("Verificacion completada");
        sendEmail.setText("Se verifico correctamente tu cuenta, bienvenido a nuestra aplicación");

        Role role = user.getRoles().iterator().next();

        String token = jwtService.generateToken(
            user.getUser_id(),
            user.getUserName(),
            role.getRoleID()
        );

        mailSender.send(sendEmail);

        responseDTO.setMessage("El registro fue exitoso");
        responseDTO.setJwt(token);

        return responseDTO;
    }

    /**
     * Este metodo permite al usuario iniciar sesión con su correo y contraseña, y
     * se le enviará un código al correo para el inicio de sesión en dos pasos.
     * 
     * @param loginRequestDTO
     * @return
     */
    public MessageResponseDTO login(LoginRequestDTO loginRequestDTO) {
        if (loginRequestDTO.getEmail() == null || loginRequestDTO.getPassword() == null) {
            throw new RuntimeException("La contraseña y el correo no pueden estar vacios");
        }

        Optional<User> optionalUser = userRepository.findByEmail(loginRequestDTO.getEmail());

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("El usuario no existe");
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("Correo o contraseña incorrectos");
        }

        String secretKey = user.getSecretKey().getSecretKey();

        Integer code = otpService.generateOtp(secretKey);

        sendEmail(loginRequestDTO.getEmail(), code);

        MessageResponseDTO response = new MessageResponseDTO();
        response.setMessage(
                "Se envio el codigo de verificación para el inicio de sesión al correo: " + loginRequestDTO.getEmail());

        return response;
    }

    public JwtResponseDTO loginSecondStep(ValidationCodeDTO validationCodeDTO){

        if(validationCodeDTO.getEmail() == null || validationCodeDTO.getCode() == null){
            throw new RuntimeException("Ningun campo puede estar vació");
        }

        User user = userRepository.findByEmail(validationCodeDTO.getEmail()).orElseThrow(()-> new RuntimeException("Usuario con ese correo inexistente"));

        Role role = user.getRoles().iterator().next();

        Boolean isValid = validateCode(validationCodeDTO);

        if(!isValid){
            throw new RuntimeException("El codigo que ingreso es incorrecto o ya expiro");
        }

        String token = jwtService.generateToken(user.getUser_id(), user.getUserName() , role.getRoleID());

        JwtResponseDTO response = new JwtResponseDTO();

        response.setJwt(token);
        response.setMessage("Se inicio sesión correctamente");

        return response;
    }

}
