package com.tourism.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cost")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cost_id")
    private Integer costId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id")
    private Tour tour;

    @Column(name = "adult_price")
    private BigDecimal adultPrice;

    @Column(name = "single_person_price")
    private BigDecimal singlePersonPrice;

    @Column(name = "extra_person_price")
    private BigDecimal extraPersonPrice;

    @Column(name = "child_with_bed_price")
    private BigDecimal childWithBedPrice;

    @Column(name = "child_without_bed_price")
    private BigDecimal childWithoutBedPrice;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "is_active")
    private Boolean isActive;
}
