package com.etour.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tour_city",
       indexes = @Index(name = "idx_tour_city_name", columnList = "city_name"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TourCity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "city_id")
    @EqualsAndHashCode.Include
    private Integer cityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id")
    @ToString.Exclude
    private Tour tour;

    @Column(name = "city_name", length = 100)
    private String cityName;

    @Column(name = "stop_order")
    private Integer stopOrder;
}
