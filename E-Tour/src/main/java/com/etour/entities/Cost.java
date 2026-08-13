package com.etour.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cost")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Cost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cost_id")
    @EqualsAndHashCode.Include
    private Integer costId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id")
    @ToString.Exclude
    private Tour tour;

    @Column(name = "adult_price", precision = 10, scale = 2)
    private BigDecimal adultPrice;

    @Column(name = "single_person_price", precision = 10, scale = 2)
    private BigDecimal singlePersonPrice;

    @Column(name = "extra_person_price", precision = 10, scale = 2)
    private BigDecimal extraPersonPrice;

    @Column(name = "child_with_bed_price", precision = 10, scale = 2)
    private BigDecimal childWithBedPrice;

    @Column(name = "child_without_bed_price", precision = 10, scale = 2)
    private BigDecimal childWithoutBedPrice;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "is_active")
    private Boolean isActive;
}
