package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "category_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "catmaster_id")
	private Integer catmasterId;

	// Business/short code referenced by Sub Category Master (see class-level note)
	@NotBlank
	@Column(name = "cat_id", nullable = false, unique = true, length = 10)
	private String catId;

	@NotBlank
	@Column(name = "cat_name", nullable = false, length = 100)
	private String catName;

	@Column(name = "cat_image_path")
	private String catImagePath;

	@Column(name = "flag")
	private Boolean flag;

	@Column(name = "isactive")
	private Boolean isActive;

	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
	@Builder.Default
	private List<SubCategoryMaster> subCategories = new ArrayList<>();

	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
	@Builder.Default
	private List<CostMaster> costs = new ArrayList<>();

	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
	@Builder.Default
	private List<DepartureMaster> departures = new ArrayList<>();

	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
	@Builder.Default
	private List<ItineraryMaster> itineraries = new ArrayList<>();

	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
	@Builder.Default
	private List<TourMaster> tours = new ArrayList<>();
}
