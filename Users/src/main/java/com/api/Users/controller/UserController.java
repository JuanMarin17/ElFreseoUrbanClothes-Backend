package com.api.Users.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

import com.api.Users.dto.MessageResponseDTO;
import com.api.Users.dto.UserDTO;
import com.api.Users.dto.UserResponseDTO;
import com.api.Users.dto.UserWithStoreDTO;
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
        Long start = System.currentTimeMillis();
        UserResponseDTO response = userService.myProfile();
        System.out.println("Users:" + (System.currentTimeMillis() - start) + "ms");
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

    /** Trae varios usuarios por ID en una sola petición (consumido por Store para evitar N+1 al listar staff). */
    @PostMapping("/batch")
    public ResponseEntity<List<UserResponseDTO>> getUsersByIds(@RequestBody List<UUID> ids) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUsersByIds(ids));
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
    /** Requiere JWT con rol ADMIN o SUPERADMIN */
    @GetMapping("/admin/all")
    public ResponseEntity<Page<UserWithStoreDTO>> listAllUsersWithStore(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userService.listAllUsersWithStore(PageRequest.of(page, size)));
    }

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
