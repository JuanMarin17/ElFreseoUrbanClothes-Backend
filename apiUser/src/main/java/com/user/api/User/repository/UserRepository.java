package com.user.api.User.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.user.api.User.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
