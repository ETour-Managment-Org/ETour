package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user", uniqueConstraints = { @UniqueConstraint(columnNames = "username") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Integer userId;

	@NotBlank
	@Column(name = "username", nullable = false, unique = true, length = 50)
	private String username;

	@NotBlank
	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Email
	@Column(name = "email", length = 100)
	private String email;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "role_id")
	private Role role;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "isactive")
	private Boolean isActive;

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
	private CustomerMaster customerMaster;

	@PrePersist
	protected void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
		if (isActive == null) {
			isActive = true;
		}
	}
}
