package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departure_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartureMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "departure_id")
	private Integer departureId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "catmaster_id")
	private CategoryMaster category;

	@Column(name = "depart_date")
	private LocalDate departDate;

	@Column(name = "end_date")
	private LocalDate endDate;

	@Column(name = "no_of_days")
	private Integer noOfDays;

	@Column(name = "isactive")
	private Boolean isActive;

	@OneToMany(mappedBy = "departure", cascade = CascadeType.ALL)
	@Builder.Default
	private List<TourMaster> tours = new ArrayList<>();

	@OneToMany(mappedBy = "departure", cascade = CascadeType.ALL)
	@Builder.Default
	private List<BookingHeader> bookings = new ArrayList<>();
}
