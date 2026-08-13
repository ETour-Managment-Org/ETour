package com.etour.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tour_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TourImages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    @EqualsAndHashCode.Include
    private Integer imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id")
    @ToString.Exclude
    private Tour tour;

    @Column(name = "source", length = 255)
    private String source;

    @Column(name = "image_title", length = 150)
    private String imageTitle;

    @Column(name = "is_primary")
    private Boolean isPrimary;

    @Column(name = "upload_date")
    private LocalDateTime uploadDate;
}
