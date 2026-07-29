package com.tourism.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tourism.demo.entity.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(String roleName);
}