package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tour_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tour_id")
    private Integer tourId;

    @NotBlank(message = "Tour name is required")
    @Size(max = 250, message = "Tour name must not exceed 250 characters")
    @Column(name = "tour_name", nullable = false, length = 250)
    private String tourName; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catmaster_id")
    private CategoryMaster category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departure_id")
    private DepartureMaster departure;

    @Builder.Default
    @Column(name = "isactive")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL)
    @Builder.Default
    private List<BookingHeader> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL)
    @Builder.Default
    private List<TourReview> reviews = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.isActive == null) {
            this.isActive = true;
        }
    }
}