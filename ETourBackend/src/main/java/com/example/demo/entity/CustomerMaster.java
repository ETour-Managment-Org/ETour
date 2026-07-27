package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cust_id")
	private Integer custId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", unique = true)
	private User user;

	@NotBlank
	@Column(name = "cust_name", nullable = false, length = 100)
	private String custName;

	@Column(name = "contact_no", length = 20)
	private String contactNo;

	@Column(name = "address")
	private String address;

	@Column(name = "isactive")
	private Boolean isActive;

	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
	@Builder.Default
	private List<BookingHeader> bookings = new ArrayList<>();

	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
	@Builder.Default
	private List<TourReview> reviews = new ArrayList<>();
}
