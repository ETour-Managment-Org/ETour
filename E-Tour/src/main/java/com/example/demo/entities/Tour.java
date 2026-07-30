package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tour")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Tour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tour_id")
    @EqualsAndHashCode.Include
    private Integer tourId;

    @Column(name = "tour_name", length = 150)
    private String tourName;

    @Column(name = "destination", length = 150)
    private String destination;

    @Column(name = "days")
    private Integer days;

    @Column(name = "nights")
    private Integer nights;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "price")
    private Float price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcat_id")
    @ToString.Exclude
    private SubCategoryMaster subCategory;

    @Column(name = "location", length = 150)
    private String location;

    @Column(name = "stay_and_meals", columnDefinition = "TEXT")
    private String stayAndMeals;

    @Column(name = "add_ons", columnDefinition = "TEXT")
    private String addOns;

    @Column(name = "passport_and_visa", columnDefinition = "TEXT")
    private String passportAndVisa;

    @Column(name = "weather", columnDefinition = "TEXT")
    private String weather;

    @Column(name = "do_and_dont", columnDefinition = "TEXT")
    private String doAndDont;

    @OneToMany(mappedBy = "tour", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<Schedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "tour", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<Itinerary> itineraries = new ArrayList<>();

    @OneToMany(mappedBy = "tour", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<Journey> journeys = new ArrayList<>();

    @OneToMany(mappedBy = "tour", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<TourImages> images = new ArrayList<>();

    @OneToMany(mappedBy = "tour", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<Cost> costs = new ArrayList<>();

    @OneToMany(mappedBy = "tour", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<Review> reviews = new ArrayList<>();
}
