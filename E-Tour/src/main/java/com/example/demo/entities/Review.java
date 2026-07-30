package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "review")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    @EqualsAndHashCode.Include
    private Integer reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    @ToString.Exclude
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @ToString.Exclude
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id")
    @ToString.Exclude
    private Tour tour;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "review_title", length = 150)
    private String reviewTitle;

    @Column(name = "review_description", length = 1000)
    private String reviewDescription;

    @Column(name = "review_date")
    private LocalDate reviewDate;

    @Column(name = "verification_status", length = 30)
    private String verificationStatus;
}
