package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "itinerary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Itinerary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "itinerary_id")
    @EqualsAndHashCode.Include
    private Integer itineraryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id")
    @ToString.Exclude
    private Tour tour;

    @Column(name = "day_number")
    private Integer dayNumber;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "location", length = 150)
    private String location;
}
