package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "passenger_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerDetail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pax_id")
	private Integer paxId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "booking_id")
	private BookingHeader booking;

	@NotBlank
	@Column(name = "pax_name", nullable = false, length = 100)
	private String paxName;

	@Column(name = "pax_birthdate")
	private LocalDate paxBirthdate;

	// e.g. 'Adult', 'Child with bed', 'Child without bed', 'Infant'
	@Column(name = "pax_type", length = 30)
	private String paxType;

	@Column(name = "pax_amount", precision = 10, scale = 2)
	private BigDecimal paxAmount;
}