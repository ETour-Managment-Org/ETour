package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sub_category_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SubCategoryMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subcat_id")
    @EqualsAndHashCode.Include
    private Integer subcatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cat_id")
    @ToString.Exclude
    private Category category;

    @Column(name = "subcat_name", length = 100)
    private String subcatName;

    @Column(name = "subcat_image_path", length = 255)
    private String subcatImagePath;

    @Column(name = "flag")
    private Boolean flag;

    @Column(name = "isactive")
    private Boolean isactive;

    @OneToMany(mappedBy = "subCategory", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<Tour> tours = new ArrayList<>();
}
