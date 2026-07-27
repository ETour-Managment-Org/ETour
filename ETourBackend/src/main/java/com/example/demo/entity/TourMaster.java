package com.example.demo.entity;

import jakarta.persistence.*;
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "catmaster_id")
	private CategoryMaster category;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "departure_id")
	private DepartureMaster departure;

	@Column(name = "isactive")
	private Boolean isActive;

	@OneToMany(mappedBy = "tour", cascade = CascadeType.ALL)
	@Builder.Default
	private List<BookingHeader> bookings = new ArrayList<>();

	@OneToMany(mappedBy = "tour", cascade = CascadeType.ALL)
	@Builder.Default
	private List<TourReview> reviews = new ArrayList<>();
}
