package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cost_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cost_id")
	private Integer costId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "catmaster_id")
	private CategoryMaster category;

	@Column(name = "cost", precision = 10, scale = 2)
	private BigDecimal cost;

	@Column(name = "single_person_cost", precision = 10, scale = 2)
	private BigDecimal singlePersonCost;

	@Column(name = "extra_person_cost", precision = 10, scale = 2)
	private BigDecimal extraPersonCost;

	@Column(name = "child_with_bed", precision = 10, scale = 2)
	private BigDecimal childWithBed;

	@Column(name = "child_without_bed", precision = 10, scale = 2)
	private BigDecimal childWithoutBed;

	@Column(name = "valid_from")
	private LocalDate validFrom;

	@Column(name = "valid_to")
	private LocalDate validTo;

	@Column(name = "isactive")
	private Boolean isActive;
}
