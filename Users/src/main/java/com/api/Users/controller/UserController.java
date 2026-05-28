package com.api.Users.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

import java.util.Map;

import com.api.Users.dto.MessageResponseDTO;
import com.api.Users.dto.UserDTO;
import com.api.Users.dto.UserResponseDTO;
import com.api.Users.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/createUser")
    public ResponseEntity<MessageResponseDTO> create(@Valid @RequestBody UserDTO userDTO) {
        MessageResponseDTO response = userService.createUser(userDTO);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/myUserName/{id}")
    public ResponseEntity<String> getNameById(@PathVariable("id") UUID id) {
        String userName = userService.getNameById(id);
        return ResponseEntity.status(HttpStatus.OK).body(userName);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> myProfile() {
        UserResponseDTO response = userService.myProfile();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/update")
    public ResponseEntity<MessageResponseDTO> updateUser(
            @RequestBody UserDTO userDTO) {

        MessageResponseDTO response = userService.updateUser(userDTO);
        return ResponseEntity.status(response.getStatus()).body(response);
    }


    @GetMapping("/getUserById/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable("id") UUID id) {
        UserResponseDTO response = userService.getUserById(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/existUser/{id}")
    public ResponseEntity<Boolean> existUser(@PathVariable UUID id){
        return ResponseEntity.status(HttpStatus.OK).body(userService.existUser(id));
    }

    /**
     * Sube la foto de perfil del usuario autenticado a Cloudinary.
     * El backend hace todo: recibe el archivo, lo sube y guarda la URL.
     *
     * PATCH /api/v1/users/profile-image
     * Content-Type: multipart/form-data
     * Body: image (file)
     */
    @PatchMapping("/profile-image")
    public ResponseEntity<Map<String, String>> uploadProfileImage(
            @RequestParam("image") MultipartFile image) {

        if (image.isEmpty()) {
            throw new RuntimeException("El archivo de imagen está vacío");
        }

        String imageUrl = userService.uploadProfileImage(image);
        return ResponseEntity.ok(Map.of("imageProfile", imageUrl));
    }
}
