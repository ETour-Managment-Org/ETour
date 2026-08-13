package com.etour.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserDTO {
    private Integer userId;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String city;
    private String gender;
    private String role;
    private Boolean active;
    private LocalDateTime createdAt;

    private Long totalBookings;
    private Long activeBookings;
    private Long cancelledBookings;
    private BigDecimal totalSpent;
}
