package com.etour.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entities.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(String roleName);
}
