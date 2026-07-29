package com.tourism.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sub_category_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubCategoryMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subcat_id")
    private Integer subcatId;

    // Mapped as a proper relationship (diagram showed cat_id as CHAR,
    // but it references Category.Category_ID)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cat_id")
    private Category category;

    @Column(name = "subcat_name")
    private String subcatName;

    @Column(name = "subcat_image_path")
    private String subcatImagePath;

    @Column(name = "flag")
    private Boolean flag;

    @Column(name = "isactive")
    private Boolean isActive;
}
