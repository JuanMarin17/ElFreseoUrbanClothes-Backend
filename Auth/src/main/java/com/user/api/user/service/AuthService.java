package com.user.api.user.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.common_request_context_starter.context.RequestContext;
import com.user.api.user.client.NotificationClient;
import com.user.api.user.client.UsersClient;
import com.user.api.user.dto.EmailRequestDTO;
import com.user.api.user.dto.ForgotPasswordRequestDTO;
import com.user.api.user.dto.JwtResponseDTO;
import com.user.api.user.dto.LoginRequestDTO;
import com.user.api.user.dto.MessageResponseDTO;
import com.user.api.user.dto.UserRegisterDTO;
import com.user.api.user.dto.UserRequestDTO;
import com.user.api.user.dto.ValidationCodeDTO;
import com.user.api.user.entity.Role;
import com.user.api.user.entity.SecretKey;
import com.user.api.user.entity.User;
import com.user.api.user.exception.BadRequestException;
import com.user.api.user.exception.IncorrectCredentialsException;
import com.user.api.user.exception.InvalidOtpException;
import com.user.api.user.exception.RoleNotFoundException;
import com.user.api.user.exception.UserAlreadyExistsException;
import com.user.api.user.exception.UserNotFoundException;
import com.user.api.user.repository.RoleRepository;
import com.user.api.user.repository.SecretKeyRepository;
import com.user.api.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final SecretKeyRepository secretKeyRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final OtpService otpService;
    private final RoleRepository roleRepository;
    private final UsersClient usersClient;
    private final UserSessionService userSessionService;
    private final NotificationClient notificationClient;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Boolean validateCode(ValidationCodeDTO validationCodeDTO) {
        Optional<User> optionalUser = findByEmail(validationCodeDTO.getEmail().toLowerCase());

        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("No se encontro un usuario con ese correo");
        }

        User user = optionalUser.get();
        SecretKey secretKey = user.getSecretKey();
        Boolean isValid = otpService.validateOtp(secretKey.getSecretKey(), validationCodeDTO.getCode());

        if (!isValid) {
            throw new InvalidOtpException("El codigo ingresado expiro o es incorrecto");
        }

        return true;
    }

    public MessageResponseDTO resendVerificationCode(EmailRequestDTO email) {
        User user = userRepository.findByEmail(email.getEmail().toLowerCase())
                .orElseThrow(() -> new UserNotFoundException("No existe una cuenta con ese correo"));

        SecretKey secretKey = secretKeyRepository.findByUser(user)
                .orElseThrow(() -> {
                    log.error("SecretKey no encontrada para usuario {}", user.getUser_id());
                    return new RuntimeException("Error de configuración interno");
                });

        String newCode = otpService.generateOtp(secretKey.getSecretKey());
        secretKey.setCode(passwordEncoder.encode(newCode));
        secretKey.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        secretKeyRepository.save(secretKey);

        emailService.sendVerificationCode(email.getEmail().toLowerCase(), newCode);

        MessageResponseDTO response = new MessageResponseDTO();
        response.setMessage("Se envió un nuevo código a: " + email.getEmail().toLowerCase());
        return response;
    }

    @Transactional(rollbackOn = Exception.class)
    public MessageResponseDTO register(UserRequestDTO userRequestDTO) {
        User user = new User();

        if (findByEmail(userRequestDTO.getEmail().toLowerCase().toLowerCase()).isPresent()) {
            throw new UserAlreadyExistsException("Usuario con este correo ya existente");
        }

        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> new RoleNotFoundException("Tipo de usuario inexistente"));

        user.setEmail(userRequestDTO.getEmail().toLowerCase().toLowerCase());
        user.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
        user.setCreateAt(LocalDateTime.now());
        user.setRoles(Set.of(role));

        User userSave = userRepository.save(user);

        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setUserId(userSave.getUser_id());
        userRegisterDTO.setUserName(userRequestDTO.getUserName());
        userRegisterDTO.setPhone(userRequestDTO.getPhone());
        userRegisterDTO.setImageProfile(userRequestDTO.getImageProfile());

        try {
            usersClient.createUser(userRegisterDTO);
        } catch (Exception e) {
            log.error("Error al crear perfil en Users para userId={}: {}", userSave.getUser_id(), e.getMessage());
            throw new RuntimeException("No se pudo completar el registro, intente de nuevo");
        }

        String userSecretKey = otpService.userSecretKey();
        SecretKey secretKey = new SecretKey();
        secretKey.setSecretKey(userSecretKey);
        secretKey.setUser(userSave);
        secretKeyRepository.save(secretKey);

        String code = otpService.generateOtp(userSecretKey);
        emailService.sendVerificationCode(userRequestDTO.getEmail().toLowerCase(), code);

        MessageResponseDTO messageResponseDTO = new MessageResponseDTO();
        messageResponseDTO.setMessage("Se envio un correo a " + userRequestDTO.getEmail().toLowerCase() + " con el codigo de verificación");
        return messageResponseDTO;
    }

    @Transactional
    public JwtResponseDTO registerSecondStep(ValidationCodeDTO validationCodeDTO, String ipAddress, String userAgent) {
        validateCode(validationCodeDTO);

        User user = userRepository.findByEmail(validationCodeDTO.getEmail().toLowerCase())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        user.setIsActive(true);
        userRepository.save(user);
        secretKeyRepository.delete(user.getSecretKey());

        Role role = user.getRoles().iterator().next();
        String userName = usersClient.getUserName(user.getUser_id());

        UUID sessionId = null;
        try {
            sessionId = userSessionService.saveSession(user.getUser_id(), ipAddress, userAgent);
        } catch (Exception e) {
            log.warn("No se pudo guardar la sesión tras el registro: {}", e.getMessage());
        }

        String token = jwtService.generateToken(user.getUser_id(), userName, role.getName(), user.getEmail().toLowerCase(), sessionId);

        emailService.sendWelcome(validationCodeDTO.getEmail().toLowerCase());

        JwtResponseDTO responseDTO = new JwtResponseDTO();
        responseDTO.setMessage("El registro fue exitoso");
        responseDTO.setJwt(token);
        return responseDTO;
    }

    public MessageResponseDTO login(LoginRequestDTO loginRequestDTO) {
        User user = userRepository.findByEmail(loginRequestDTO.getEmail().toLowerCase())
                .orElseThrow(() -> new IncorrectCredentialsException("Credenciales incorrectas"));

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new IncorrectCredentialsException("Credenciales incorrectas");
        }

        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
            throw new IncorrectCredentialsException("Credenciales incorrectas");
        }

        String code = otpService.generateOtp(user.getSecretKey().getSecretKey());
        emailService.sendVerificationCode(loginRequestDTO.getEmail().toLowerCase(), code);

        MessageResponseDTO response = new MessageResponseDTO();
        response.setMessage("Se envio el codigo de verificación para el inicio de sesión al correo: " + loginRequestDTO.getEmail().toLowerCase());
        return response;
    }

    public JwtResponseDTO loginSecondStep(ValidationCodeDTO validationCodeDTO, String ipAddress, String userAgent) {
        User user = userRepository.findByEmail(validationCodeDTO.getEmail().toLowerCase())
                .orElseThrow(() -> new IncorrectCredentialsException("Credenciales incorrectas"));

        Role role = user.getRoles().iterator().next();

        if (!validateCode(validationCodeDTO)) {
            throw new InvalidOtpException("El codigo que ingreso es incorrecto o ya expiro");
        }

        String userName = usersClient.getUserName(user.getUser_id());

        UUID sessionId = null;
        try {
            sessionId = userSessionService.saveSession(user.getUser_id(), ipAddress, userAgent);
        } catch (Exception e) {
            log.warn("No se pudo guardar la sesión tras el login: {}", e.getMessage());
        }

        String token = jwtService.generateToken(user.getUser_id(), userName, role.getName(), user.getEmail().toLowerCase(), sessionId);

        emailService.sendNewLoginAlert(user.getEmail().toLowerCase(), ipAddress, userAgent);
        notificationClient.sendSessionAlert(user.getUser_id(), ipAddress, userAgent);

        JwtResponseDTO response = new JwtResponseDTO();
        response.setJwt(token);
        response.setMessage("Se inicio sesión correctamente");
        return response;
    }

    public MessageResponseDTO forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        String code = otpService.generateOtp(user.getSecretKey().getSecretKey());
        emailService.sendVerificationCode(email, code);

        MessageResponseDTO response = new MessageResponseDTO();
        response.setMessage("Se envio un codigo de verificación al correo: " + email);
        return response;
    }

    @Transactional
    public MessageResponseDTO forgotPasswordSecondStep(ForgotPasswordRequestDTO fPRequest) {
        User user = userRepository.findByEmail(fPRequest.getEmail().toLowerCase())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        Boolean isValid = otpService.validateOtp(user.getSecretKey().getSecretKey(), fPRequest.getCode());

        if (!isValid) {
            throw new InvalidOtpException("El codigo ingresado es incorrecto o ya expiro");
        }

        user.setPassword(passwordEncoder.encode(fPRequest.getPassword()));
        userRepository.save(user);

        emailService.sendPasswordChanged(fPRequest.getEmail().toLowerCase());

        MessageResponseDTO responseDTO = new MessageResponseDTO();
        responseDTO.setMessage("Se cambio la contraseña correctamente");
        return responseDTO;
    }

    public MessageResponseDTO deactivateAccount() {
        String userIdHeader = RequestContext.getHeader("X-User-Id");

        if (userIdHeader == null) {
            throw new IncorrectCredentialsException("No autenticado");
        }

        UUID userId;
        try {
            userId = UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Formato inválido del userId");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        user.setIsActive(false);
        userRepository.save(user);

        MessageResponseDTO response = new MessageResponseDTO();
        response.setMessage("Cuenta desactivada correctamente");
        return response;
    }

    public String getEmailByUserId(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"))
                .getEmail().toLowerCase();
    }

    public com.user.api.user.dto.UserInfoResponseDTO getUserInfo(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        return new com.user.api.user.dto.UserInfoResponseDTO(
                user.getUser_id(),
                user.getEmail().toLowerCase(),
                user.getCreateAt());
    }

    public JwtResponseDTO refreshToken(String token) {
        String newToken = jwtService.refrechToken(token);
        JwtResponseDTO response = new JwtResponseDTO();
        response.setJwt(newToken);
        response.setMessage("Token renovado correctamente");
        return response;
    }
}
