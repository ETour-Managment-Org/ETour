package com.etour.dto.category;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubCategoryDTO {
    private Integer subcatId;
    private String subcatName;
    private String subcatImagePath;

    private Integer categoryId;
    private String categoryName;

    private Long tourCount;
}
