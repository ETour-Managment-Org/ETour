package com.tourism.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tourism.demo.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Optional<User> findByEmail(String email);
}