package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    // Expected values: 'User', 'Admin'
    @Column(name = "role_name", nullable = false, length = 20)
    private String roleName;

    @Column(name = "description")
    private String description;
}