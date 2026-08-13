package com.etour.entities;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    @EqualsAndHashCode.Include
    private Integer feedbackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    @Column(name = "name", length = 120)
    private String name;

    @Column(name = "email", length = 120)
    private String email;

    @Column(name = "category", length = 30)
    private String category;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "message", length = 2000)
    private String message;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "page_url", length = 255)
    private String pageUrl;

    @Column(name = "published")
    private Boolean published;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
