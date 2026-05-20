package com.user.api.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.user.api.user.dto.JwtResponseDTO;
import com.user.api.user.dto.LoginRequestDTO;
import com.user.api.user.dto.MessageResponseDTO;
import com.user.api.user.dto.EmailRequestDTO;
import com.user.api.user.dto.ForgotPasswordRequestDTO;
import com.user.api.user.dto.UserRequestDTO;
import com.user.api.user.dto.ValidationCodeDTO;
// import com.user.api.user.dto.UserResponseDTO;
import com.user.api.user.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponseDTO> register(@Valid @RequestBody UserRequestDTO userRequestDTO) {

        MessageResponseDTO response = authService.register(userRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PostMapping("/registerSecondStep")
    public ResponseEntity<JwtResponseDTO> registerSecondStep(
            @Valid @RequestBody ValidationCodeDTO verificationCodeDTO) {

        JwtResponseDTO response = authService.registerSecondStep(verificationCodeDTO);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

    }

    @PostMapping("/resendVerificationCode")
    public ResponseEntity<MessageResponseDTO> resendCode(@Valid @RequestBody EmailRequestDTO resendCodeDTO) {

        MessageResponseDTO response = authService.resendVerificationCode(resendCodeDTO);
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    @PostMapping("/login")
    public ResponseEntity<MessageResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {

        MessageResponseDTO responseDTO = authService.login(loginRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);

    }

    @PostMapping("/loginSecondStep")
    public ResponseEntity<JwtResponseDTO> loginSecondStep(@Valid @RequestBody ValidationCodeDTO validationCodeDTO) {

        JwtResponseDTO responseDTO = authService.loginSecondStep(validationCodeDTO);
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);

    }

    @PostMapping("/forgotPassword")
    public ResponseEntity<MessageResponseDTO> forgotPassword(@Valid @RequestBody EmailRequestDTO emailRequestDTO){
        MessageResponseDTO responseDTO = authService.forgotPassword(emailRequestDTO.getEmail());
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }

    @PutMapping("/forgotPasswordSecondStep")
    public ResponseEntity<MessageResponseDTO> forgotPasswordSecondStep(@Valid @RequestBody ForgotPasswordRequestDTO forgotPasswordRequestDTO){
        MessageResponseDTO responseDTO = authService.forgotPasswordSecondStep(forgotPasswordRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }
    @DeleteMapping("/deactivateAccount")
    public ResponseEntity<MessageResponseDTO> deactivateAccount() {
        MessageResponseDTO response = authService.deactivateAccount();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
