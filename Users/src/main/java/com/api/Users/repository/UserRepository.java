package com.api.Users.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.Users.entity.User;

public interface UserRepository extends JpaRepository<User, UUID>{ 
    Optional<User> findByUserName(String userName);
}
