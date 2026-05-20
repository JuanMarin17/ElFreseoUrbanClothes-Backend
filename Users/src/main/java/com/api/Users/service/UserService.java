package com.api.Users.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.api.Users.dto.MessageResponseDTO;
import com.api.Users.dto.UserDTO;
import com.api.Users.entity.User;
import com.api.Users.exception.BadRequestException;
import com.api.Users.exception.UnauthorizedUserException;
import com.api.Users.exception.UserNotFoundException;
import com.api.Users.repository.UserRepository;
import com.common_request_context_starter.context.RequestContext;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Data
public class UserService {
    private final UserRepository userRepository;

    /**
     * Metodo para crear usuario con foto de perfil
     * 
     * @param user
     * @return
     */
    public MessageResponseDTO createUser(UserDTO user) {
        if (userRepository.findByUserName(user.getUserName()).isPresent()) {
            throw new BadRequestException("Nombre de usuario ya existente");
        }

        User userEntity = new User();
        userEntity.setUserId(user.getUserId());
        userEntity.setUserName(user.getUserName());
        userEntity.setPhone(user.getPhone());
        userEntity.setImageProfile(user.getImageProfile());

        User createdUser = userRepository.save(userEntity);

        if (createdUser == null) {
            throw new RuntimeException("No se creo el usuario correctamente");
        }

        MessageResponseDTO response = new MessageResponseDTO();
        response.setMessage("Usuario creado correctamente");
        response.setStatus(201);

        return response;
    }

    /**
     * Obtiene el nombre de un usuario a partir de su ID.
     *
     * @param userId identificador único del usuario
     * @return nombre del usuario encontrado
     */
    public String getNameById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        String userName = user.getUserName();

        return userName;
    }

    /**
     * Obtiene la información del perfil del usuario autenticado
     * usando el ID enviado en el header X-User-Id.
     *
     * @return datos básicos del usuario autenticado
     */
    public UserDTO myProfile() {
        String userIdHeader = RequestContext.getHeader("X-User-Id");

        if (userIdHeader == null) {
            throw new UnauthorizedUserException("Usuario no autenticado");
        }

        UUID userId;
        try {
            userId = UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Formato invalido del userId");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        UserDTO response = new UserDTO();

        response.setUserId(userId);
        response.setUserName(user.getUserName());
        response.setPhone(user.getPhone());
        response.setImageProfile(user.getImageProfile());

        return response;
    }

    /**
     * Metodo para actualizar la información del usuario, verifica que se haya
     * enviado una iamgen
     * si no se envia imagen toma la anterior
     * 
     * @param userId
     * @param userDTO
     * @return
     */
    public MessageResponseDTO updateUser(UUID userId, UserDTO userDTO) {

        User userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if (userDTO.getUserName() != null &&
                !userDTO.getUserName().equals(userEntity.getUserName())) {

            if (userRepository.findByUserName(userDTO.getUserName()).isPresent()) {
                throw new BadRequestException("El nombre de usuario ya existe");
            }

            userEntity.setUserName(userDTO.getUserName());
        }

        if (userDTO.getPhone() != null) {
            userEntity.setPhone(userDTO.getPhone());
        }

        if (userDTO.getImageProfile() != null) {
            userEntity.setImageProfile(userDTO.getImageProfile());
        }

        userRepository.save(userEntity);

        MessageResponseDTO response = new MessageResponseDTO();
        response.setMessage("Usuario actualizado correctamente");
        response.setStatus(200);

        return response;
    }

    /**
     * Obtiene la información de un usuario a partir de su ID.
     *
     * @param userId
     * @return datos básicos del usuario encontrado
     */
    public UserDTO getUserById(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        UserDTO response = new UserDTO();
        response.setUserId(user.getUserId());
        response.setUserName(user.getUserName());
        response.setPhone(user.getPhone());
        response.setImageProfile(user.getImageProfile());

        return response;
    }

}
