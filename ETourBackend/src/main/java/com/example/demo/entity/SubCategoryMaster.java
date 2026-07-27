package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "sub_category_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubCategoryMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "subcatmaster_id")
	private Integer subcatmasterId;

	@Column(name = "subcat_id", length = 10)
	private String subcatId;

	// Diagram references CategoryMaster.cat_id (business code), not the PK
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cat_id", referencedColumnName = "cat_id")
	private CategoryMaster category;

	@NotBlank
	@Column(name = "subcat_name", nullable = false, length = 100)
	private String subcatName;

	@Column(name = "subcat_image_path")
	private String subcatImagePath;

	@Column(name = "flag")
	private Boolean flag;

	@Column(name = "isactive")
	private Boolean isActive;
}
