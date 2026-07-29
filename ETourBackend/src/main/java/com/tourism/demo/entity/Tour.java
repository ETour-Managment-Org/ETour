package com.tourism.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tour")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tour_id")
    private Integer tourId;

    @Column(name = "tour_name")
    private String tourName;

    @Column(name = "destination")
    private String destination;

    @Column(name = "days")
    private Integer days;

    @Column(name = "nights")
    private Integer nights;

    @Column(name = "description")
    private String description;

    @Column(name = "price")
    private Double price;

    // Diagram showed Category as VARCHAR FK; mapped as relationship
    // to Category.Category_ID for referential integrity.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "location")
    private String location;
}
