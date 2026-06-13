package com.api.Users.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.api.Users.client.AuthClient;
import com.api.Users.client.StoreClient;
import com.api.Users.dto.MessageResponseDTO;
import com.api.Users.dto.UserDTO;
import com.api.Users.dto.UserResponseDTO;
import com.api.Users.dto.UserWithStoreDTO;
import com.api.Users.entity.User;
import com.api.Users.exception.BadRequestException;
import com.api.Users.exception.UnauthorizedUserException;
import com.api.Users.exception.UserNotFoundException;
import com.api.Users.repository.UserRepository;
import com.common_request_context_starter.context.RequestContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AuthClient authClient;
    private final com.api.Users.client.MediaServiceClient mediaServiceClient;
    private final StoreClient storeClient;

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
    public UserResponseDTO myProfile() {
        String userIdHeader = RequestContext.getHeader("x-user-id");

        if (userIdHeader == null) {
            throw new UnauthorizedUserException("Usuario no autenticado");
        }

        UUID userId;
        try {
            userId = UUID.fromString(userIdHeader);
            System.out.println(userId);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Formato invalido del userId");
        }

        String userEmail = authClient.getEmail(userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        UserResponseDTO response = new UserResponseDTO();

        response.setUserId(userId);
        response.setUserName(user.getUserName());
        response.setPhone(user.getPhone());
        response.setUserEmail(userEmail);
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
    public MessageResponseDTO updateUser(UserDTO userDTO) {
        String userIdHeader = RequestContext.getHeader("x-user-id");

        UUID userId;
        try{
            userId = UUID.fromString(userIdHeader);
        } catch(Exception e){
            throw new RuntimeException("Formato de id invalido " + e.getMessage());
        }

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
    public UserResponseDTO getUserById(UUID userId) {

        if(userId == null){
            throw new RuntimeException("El id del usuario es obligatorio");
        }

        String userEmail = authClient.getEmail(userId);

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        UserResponseDTO response = new UserResponseDTO();

        response.setUserId(userId);
        response.setUserName(user.getUserName());
        response.setPhone(user.getPhone());
        response.setUserEmail(userEmail);
        response.setImageProfile(user.getImageProfile());

        return response;
    }

    public Boolean existUser(UUID id){
        Optional<User> user = userRepository.findById(id);

        if(user.isEmpty()){
            return false;
        }

        return true;
    }

    public List<UserWithStoreDTO> listAllUsersWithStore() {
        String role = RequestContext.getHeader("X-User-Role");
        if (!"ADMIN".equals(role) && !"SUPERADMIN".equals(role))
            throw new UnauthorizedUserException("Solo ADMIN o SUPERADMIN pueden acceder a este recurso");

        List<User> users = userRepository.findAll();

        // 1 HTTP call — mapa ownerId → tienda
        Map<UUID, StoreClient.StoreInfo> storeByOwner = storeClient.getAllStores()
                .stream()
                .filter(s -> s.getOwnerId() != null)
                .collect(Collectors.toMap(
                        StoreClient.StoreInfo::getOwnerId,
                        Function.identity(),
                        (a, b) -> a));

        return users.stream().map(user -> {
            StoreClient.StoreInfo store = storeByOwner.get(user.getUserId());
            return UserWithStoreDTO.builder()
                    .userId(user.getUserId())
                    .userName(user.getUserName())
                    .phone(user.getPhone())
                    .imageProfile(user.getImageProfile())
                    .hasStore(store != null)
                    .store(store != null ? UserWithStoreDTO.StoreDTO.builder()
                            .storeId(store.getStoreId())
                            .name(store.getName())
                            .slug(store.getSlug())
                            .isActive(store.getIsActive())
                            .build() : null)
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Sube la foto de perfil del usuario autenticado a Cloudinary,
     * guarda la URL en la base de datos y la devuelve.
     */
    public String uploadProfileImage(org.springframework.web.multipart.MultipartFile file) {
        String userIdHeader = RequestContext.getHeader("x-user-id");

        if (userIdHeader == null) {
            throw new UnauthorizedUserException("Usuario no autenticado");
        }

        UUID userId;
        try {
            userId = UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Formato inválido del userId");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        String imageUrl = mediaServiceClient.upload(file, "users/profiles");
        user.setImageProfile(imageUrl);
        userRepository.save(user);

        return imageUrl;
    }

}
