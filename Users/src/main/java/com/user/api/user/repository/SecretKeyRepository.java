package com.user.api.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.user.api.user.entity.SecretKey;
import com.user.api.user.entity.User;

public interface SecretKeyRepository extends JpaRepository<SecretKey, Long>{
    Optional<SecretKey> findBySecretKey(String secretKey);

    Optional<SecretKey> findByUser(User user);
}
