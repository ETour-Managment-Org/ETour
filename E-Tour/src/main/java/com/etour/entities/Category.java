package com.etour.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    @EqualsAndHashCode.Include
    private Integer categoryId;

    @Column(name = "cat_code", length = 10)
    private String catCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @ToString.Exclude
    private Category parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<Category> children = new ArrayList<>();

    @Column(name = "category_name", length = 100)
    private String categoryName;

    @Column(name = "cat_image_path", length = 255)
    private String catImagePath;

    @Column(name = "flag")
    private Boolean flag;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "suitable_for", length = 100)
    private String suitableFor;

    @Column(name = "status", length = 20)
    private String status;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<SubCategoryMaster> subCategories = new ArrayList<>();

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<Tour> tours = new ArrayList<>();

    @Transient
    public boolean isLeaf() {
        return Boolean.TRUE.equals(flag);
    }

    @Transient
    public boolean isRoot() {
        return parent == null;
    }
}
