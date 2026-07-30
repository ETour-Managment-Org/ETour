package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "journey")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Journey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "journey_id")
    @EqualsAndHashCode.Include
    private Integer journeyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id")
    @ToString.Exclude
    private Tour tour;

    @Column(name = "source", length = 150)
    private String source;

    @Column(name = "destination", length = 150)
    private String destination;

    @Column(name = "transport", length = 100)
    private String transport;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;
}
