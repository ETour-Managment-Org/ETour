package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "itinerary_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "itr_id")
	private Integer itrId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "catmaster_id")
	private CategoryMaster category;

	@Column(name = "day_no")
	private Integer dayNo;

	@Column(name = "itr_detail", columnDefinition = "TEXT")
	private String itrDetail;

	@Column(name = "isactive")
	private Boolean isActive;
}
