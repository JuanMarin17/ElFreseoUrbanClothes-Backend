package com.user.api.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.user.api.user.dto.JwtResponseDTO;
import com.user.api.user.dto.LoginRequestDTO;
import com.user.api.user.dto.MessageResponseDTO;
import com.user.api.user.dto.EmailRequestDTO;
import com.user.api.user.dto.UserRequestDTO;
import com.user.api.user.dto.ValidationCodeDTO;
// import com.user.api.user.dto.UserResponseDTO;
import com.user.api.user.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponseDTO> register(@RequestBody UserRequestDTO userRequestDTO) {
        try {
            MessageResponseDTO response = authService.register(userRequestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            MessageResponseDTO error = new MessageResponseDTO();
            error.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/registerSecondStep")
    public ResponseEntity<JwtResponseDTO> registerSecondStep(@RequestBody ValidationCodeDTO verificationCodeDTO) {
        try {
            JwtResponseDTO response = authService.registerSecondStep(verificationCodeDTO);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } catch (RuntimeException e) {
            JwtResponseDTO error = new JwtResponseDTO();
            error.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/resendVerificationCode")
    public ResponseEntity<MessageResponseDTO> resendCode(@RequestBody EmailRequestDTO resendCodeDTO) {
        try {
            MessageResponseDTO response = authService.resendVerificationCode(resendCodeDTO.getEmail());
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            MessageResponseDTO error = new MessageResponseDTO();
            error.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<MessageResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO){

        try{
            MessageResponseDTO responseDTO = authService.login(loginRequestDTO);
            return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
        } catch (RuntimeException e){
            MessageResponseDTO error = new MessageResponseDTO();
            error.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/loginSecondStep")
    public ResponseEntity<JwtResponseDTO> loginSecondStep(@RequestBody ValidationCodeDTO validationCodeDTO){

        try{
            JwtResponseDTO responseDTO = authService.loginSecondStep(validationCodeDTO);
            return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
        } catch (RuntimeException e){
            JwtResponseDTO error = new JwtResponseDTO();
            error.setMessage(e.getMessage());
            error.setJwt(null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}
