package com.api.Users.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import jakarta.validation.Valid;

import com.api.Users.dto.MessageResponseDTO;
import com.api.Users.dto.UserDTO;
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
    public ResponseEntity<String> getNameById(@PathVariable("id") UUID id){
        String userName = userService.getNameById(id);
        return ResponseEntity.status(HttpStatus.OK).body(userName);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> myProfile(){
        UserDTO response = userService.myProfile();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
