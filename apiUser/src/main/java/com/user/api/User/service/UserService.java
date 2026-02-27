package com.user.api.User.service;

import org.springframework.stereotype.Service;

import com.user.api.User.dto.UserRequestDTO;
import com.user.api.User.dto.UserResponseDTO;
import com.user.api.User.entity.User;
import com.user.api.User.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        User user = new User();

        user.setName(userRequestDTO.getName());
        user.setLastName(userRequestDTO.getLastName());
        user.setEmail(userRequestDTO.getEmail());
        user.setPassword(userRequestDTO.getPassword());
        user.setPhone(userRequestDTO.getPhone());

        if (user.getName() == null ||
                user.getLastName() == null ||
                user.getEmail() == null ||
                user.getPassword() == null ||
                user.getPhone() == null) {
            throw new IllegalArgumentException("Todos los campos son obligatorios");
        }

        userRepository.save(user);

        UserResponseDTO response = new UserResponseDTO();

        response.setUser_id(user.getUser_id());
        response.setName(user.getName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());

        return response;
    }
}
