package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tour_review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourReview {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "review_id")
	private Integer reviewId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tour_id")
	private TourMaster tour;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cust_id")
	private CustomerMaster customer;

	@Min(1)
	@Max(5)
	@Column(name = "rating")
	private Integer rating;

	@Column(name = "comments", columnDefinition = "TEXT")
	private String comments;

	@Column(name = "review_date")
	private LocalDateTime reviewDate;

	@Column(name = "isactive")
	private Boolean isActive;

	@PrePersist
	protected void onCreate() {
		if (reviewDate == null) {
			reviewDate = LocalDateTime.now();
		}
	}
}
