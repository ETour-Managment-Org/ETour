package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @Size(max = 10, message = "Sub-category ID must not exceed 10 characters")
    @Column(name = "subcat_id", length = 10)
    private String subcatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cat_id", referencedColumnName = "cat_id")
    private CategoryMaster category;

    @NotBlank(message = "Sub-category name is required")
    @Size(max = 100, message = "Sub-category name must not exceed 100 characters")
    @Column(name = "subcat_name", nullable = false, length = 100)
    private String subcatName;

    @Column(name = "subcat_image_path")
    private String subcatImagePath;

    @Builder.Default
    @Column(name = "flag")
    private Boolean flag = true;

    @Builder.Default
    @Column(name = "isactive")
    private Boolean isActive = true;

    @PrePersist
    public void prePersist() {
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.flag == null) {
            this.flag = true;
        }
    }
}